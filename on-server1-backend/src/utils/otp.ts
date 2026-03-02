import crypto from 'crypto';

export function generateOtp(length: number = 6): string {
  const digits = '0123456789';
  let otp = '';
  for (let i = 0; i < length; i++) {
    otp += digits[crypto.randomInt(0, digits.length)];
  }
  return otp;
}

export function getOtpExpiry(minutes: number): Date {
  return new Date(Date.now() + minutes * 60 * 1000);
}
