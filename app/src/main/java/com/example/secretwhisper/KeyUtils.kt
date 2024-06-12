package com.example.secretwhisper

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

fun generateKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(2048)
    return keyGen.generateKeyPair()
}

fun saveKeysToSharedPreferences(context: Context, privateKey: PrivateKey, publicKey: PublicKey) {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    sharedPreferences.edit()
        .putString("private_key", Base64.encodeToString(privateKey.encoded, Base64.DEFAULT))
        .putString("public_key", Base64.encodeToString(publicKey.encoded, Base64.DEFAULT))
        .apply()
}
