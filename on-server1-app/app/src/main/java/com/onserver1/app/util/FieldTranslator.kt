package com.onserver1.app.util

/**
 * Translates product field names from English to Arabic.
 * Used when the app language is Arabic.
 */
object FieldTranslator {

    private val translations = mapOf(
        // IMEI variants
        "imei" to "رقم IMEI",
        "imei/sn" to "IMEI / سيريل",
        "sn/imei" to "سيريل / IMEI",

        // Serial Number variants
        "sn" to "سيريل",
        "serial_number" to "سيريل",
        "serial no" to "سيريل",
        "serialno" to "سيريل",
        "serial number" to "سيريل",
        "serial" to "سيريل",
        "serail number" to "سيريل",

        // Email variants
        "email" to "البريد الإلكتروني",
        "your email" to "بريدك الإلكتروني",
        "serial_email" to "البريد الإلكتروني",
        "user/email" to "المستخدم / البريد الإلكتروني",
        "emial" to "البريد الإلكتروني",

        // Username variants
        "username" to "اسم المستخدم",
        "user" to "المستخدم",
        "targetlogin" to "اسم المستخدم المستهدف",

        // Password variants
        "password" to "كلمة المرور",
        "pass" to "كلمة المرور",
        "targetpassword" to "كلمة المرور المستهدفة",
        "team passowrd" to "كلمة مرور TeamViewer",

        // Lock Code variants
        "lock code" to "رمز القفل",
        "lockcode" to "رمز القفل",
        "lock code/ imei" to "رمز القفل / IMEI",
        "lock code/imei" to "رمز القفل / IMEI",

        // ECID
        "ecid" to "رقم ECID",

        // TeamViewer / Remote
        "team viewer" to "معرّف TeamViewer",
        "teamviewer" to "معرّف TeamViewer",
        "team id" to "معرّف TeamViewer",
        "anydesk id" to "معرّف AnyDesk",

        // Player ID variants
        "player -id" to "معرّف اللاعب",
        "player id" to "معرّف اللاعب",
        "player_id" to "معرّف اللاعب",
        "playerid" to "معرّف اللاعب",

        // Model
        "model" to "الموديل",
        "brand+model" to "الماركة + الموديل",

        // Type
        "type" to "النوع",
        "usertype" to "نوع المستخدم",

        // Code variants
        "code" to "الرمز",
        "token" to "الرمز المميز",
        "pin code" to "رمز PIN",

        // Cases
        "cases id 1" to "معرّف الحالة 1",
        "cases id 2" to "معرّف الحالة 2",
        "cases id 3" to "معرّف الحالة 3",
        "cases id 4" to "معرّف الحالة 4",

        // Hardware
        "hardware id" to "معرّف الجهاز",
        "kit id" to "معرّف العدة",

        // Other
        "notes" to "ملاحظات",
        "phone number" to "رقم الهاتف",
        "profile name" to "اسم الحساب",
        "checker report" to "تقرير الفحص",
    )

    /**
     * Translate a field name to Arabic if a translation exists.
     * Uses case-insensitive matching.
     */
    fun translate(fieldName: String): String {
        val key = fieldName.trim().lowercase()
        return translations[key] ?: fieldName
    }
}
