import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';
import prisma from '../config/database';
import { env } from '../config/env';
import { generateAccessToken, generateRefreshToken } from '../utils/jwt';
import { generateOtp, getOtpExpiry } from '../utils/otp';
import { sendWelcomeEmail, sendOtpEmail, sendPasswordResetSuccessEmail } from './emailService';

export class AuthService {
  async register(data: { email: string; password: string; name: string; phone?: string }) {
    const existingUser = await prisma.user.findUnique({ where: { email: data.email } });
    if (existingUser) {
      // If user exists but not verified, resend OTP
      if (!existingUser.isVerified) {
        const otpCode = generateOtp();
        await prisma.otpCode.create({
          data: {
            code: otpCode,
            type: 'EMAIL_VERIFICATION',
            expiresAt: getOtpExpiry(env.OTP_EXPIRY_MINUTES),
            userId: existingUser.id,
          },
        });
        console.log(`[OTP] Re-sent verification code for ${data.email}: ${otpCode}`);
        sendOtpEmail(data.email, existingUser.name, otpCode, 'EMAIL_VERIFICATION').catch(() => {});
        return { userId: existingUser.id, message: 'تم إعادة إرسال رمز التأكيد' };
      }
      throw Object.assign(new Error('Email already registered'), { statusCode: 409 });
    }

    const hashedPassword = await bcrypt.hash(data.password, 12);
    const user = await prisma.user.create({
      data: {
        email: data.email,
        password: hashedPassword,
        name: data.name,
        phone: data.phone,
        isVerified: false,
      },
    });

    // Generate OTP and send via email
    const otpCode = generateOtp();
    await prisma.otpCode.create({
      data: {
        code: otpCode,
        type: 'EMAIL_VERIFICATION',
        expiresAt: getOtpExpiry(env.OTP_EXPIRY_MINUTES),
        userId: user.id,
      },
    });

    console.log(`[OTP] Verification code for ${data.email}: ${otpCode}`);
    sendOtpEmail(data.email, data.name, otpCode, 'EMAIL_VERIFICATION').catch(() => {});

    return {
      userId: user.id,
      message: 'تم التسجيل بنجاح. تحقق من بريدك الإلكتروني لرمز التأكيد.',
    };
  }

  async login(email: string, password: string) {
    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) {
      throw Object.assign(new Error('Invalid email or password'), { statusCode: 401 });
    }

    if (!user.isActive) {
      throw Object.assign(new Error('Account is deactivated'), { statusCode: 403 });
    }

    if (!user.isVerified) {
      throw Object.assign(new Error('يرجى تأكيد بريدك الإلكتروني أولاً'), { statusCode: 403 });
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      throw Object.assign(new Error('Invalid email or password'), { statusCode: 401 });
    }

    const tokenPayload = { userId: user.id, role: user.role };
    const accessToken = generateAccessToken(tokenPayload);
    const refreshToken = generateRefreshToken(tokenPayload);

    // Store refresh token
    await prisma.refreshToken.create({
      data: {
        token: refreshToken,
        userId: user.id,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      },
    });

    return {
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        avatar: user.avatar,
        balance: user.balance,
        role: user.role,
        isVerified: user.isVerified,
      },
      accessToken,
      refreshToken,
    };
  }

  async verifyOtp(userId: string, code: string, type: 'EMAIL_VERIFICATION' | 'PHONE_VERIFICATION' | 'PASSWORD_RESET') {
    const otpRecord = await prisma.otpCode.findFirst({
      where: {
        userId,
        code,
        type,
        isUsed: false,
        expiresAt: { gt: new Date() },
      },
    });

    if (!otpRecord) {
      throw Object.assign(new Error('Invalid or expired OTP'), { statusCode: 400 });
    }

    await prisma.otpCode.update({
      where: { id: otpRecord.id },
      data: { isUsed: true },
    });

    if (type === 'EMAIL_VERIFICATION') {
      const user = await prisma.user.update({
        where: { id: userId },
        data: { isVerified: true },
      });

      // Send welcome email after verification
      sendWelcomeEmail(user.email, user.name).catch(() => {});

      // Auto-login: return tokens so the app can go straight to Home
      const tokenPayload = { userId: user.id, role: user.role };
      const accessToken = generateAccessToken(tokenPayload);
      const refreshToken = generateRefreshToken(tokenPayload);

      await prisma.refreshToken.create({
        data: {
          token: refreshToken,
          userId: user.id,
          expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        },
      });

      return {
        message: 'OTP verified successfully',
        user: {
          id: user.id,
          email: user.email,
          name: user.name,
          avatar: user.avatar,
          balance: user.balance,
          role: user.role,
          isVerified: user.isVerified,
        },
        accessToken,
        refreshToken,
      };
    }

    return { message: 'OTP verified successfully' };
  }

  async resendOtp(userId: string, type: 'EMAIL_VERIFICATION' | 'PHONE_VERIFICATION' | 'PASSWORD_RESET') {
    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) {
      throw Object.assign(new Error('User not found'), { statusCode: 404 });
    }

    const otpCode = generateOtp();
    await prisma.otpCode.create({
      data: {
        code: otpCode,
        type,
        expiresAt: getOtpExpiry(env.OTP_EXPIRY_MINUTES),
        userId: user.id,
      },
    });

    console.log(`[OTP] New code for ${user.email}: ${otpCode}`);

    // Send OTP via email (fire-and-forget)
    sendOtpEmail(user.email, user.name, otpCode, type).catch(() => {});

    return { message: 'OTP sent successfully' };
  }

  async refreshTokens(refreshToken: string) {
    const storedToken = await prisma.refreshToken.findUnique({
      where: { token: refreshToken },
      include: { user: true },
    });

    if (!storedToken || storedToken.expiresAt < new Date()) {
      throw Object.assign(new Error('Invalid refresh token'), { statusCode: 401 });
    }

    // Delete old refresh token
    await prisma.refreshToken.delete({ where: { id: storedToken.id } });

    const tokenPayload = { userId: storedToken.user.id, role: storedToken.user.role };
    const newAccessToken = generateAccessToken(tokenPayload);
    const newRefreshToken = generateRefreshToken(tokenPayload);

    await prisma.refreshToken.create({
      data: {
        token: newRefreshToken,
        userId: storedToken.user.id,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      },
    });

    return { accessToken: newAccessToken, refreshToken: newRefreshToken };
  }

  async logout(refreshToken: string) {
    await prisma.refreshToken.deleteMany({ where: { token: refreshToken } });
    return { message: 'Logged out successfully' };
  }

  async forgotPassword(email: string) {
    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) {
      // Don't reveal if email exists - still return success
      return { message: 'If the email exists, a reset code has been sent', userId: null };
    }

    const otpCode = generateOtp();
    await prisma.otpCode.create({
      data: {
        code: otpCode,
        type: 'PASSWORD_RESET',
        expiresAt: getOtpExpiry(env.OTP_EXPIRY_MINUTES),
        userId: user.id,
      },
    });

    console.log(`[OTP] Password reset code for ${email}: ${otpCode}`);

    // Send OTP via email (fire-and-forget)
    sendOtpEmail(user.email, user.name, otpCode, 'PASSWORD_RESET').catch(() => {});

    return { message: 'Reset code sent to your email', userId: user.id };
  }

  async resetPassword(userId: string, code: string, newPassword: string) {
    // Verify OTP first
    const otpRecord = await prisma.otpCode.findFirst({
      where: {
        userId,
        code,
        type: 'PASSWORD_RESET',
        isUsed: false,
        expiresAt: { gt: new Date() },
      },
    });

    if (!otpRecord) {
      throw Object.assign(new Error('Invalid or expired reset code'), { statusCode: 400 });
    }

    // Mark OTP as used
    await prisma.otpCode.update({
      where: { id: otpRecord.id },
      data: { isUsed: true },
    });

    // Hash new password and update
    const hashedPassword = await bcrypt.hash(newPassword, 12);
    await prisma.user.update({
      where: { id: userId },
      data: { password: hashedPassword },
    });

    // Invalidate all refresh tokens for security
    await prisma.refreshToken.deleteMany({ where: { userId } });

    // Send confirmation email
    const resetUser = await prisma.user.findUnique({ where: { id: userId }, select: { email: true, name: true } });
    if (resetUser) sendPasswordResetSuccessEmail(resetUser.email, resetUser.name).catch(() => {});

    return { message: 'Password reset successfully' };
  }
}

export const authService = new AuthService();
