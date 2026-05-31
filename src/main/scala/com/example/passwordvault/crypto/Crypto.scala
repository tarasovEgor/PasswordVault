package com.example.passwordvault.crypto

import cats.effect.IO

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}

trait Crypto {

  def encrypt(plainText: String): IO[String]

  def decrypt(cipherText: String): IO[String]
}

object Crypto {

  def aesGcm(masterKeyBase64: String): IO[Crypto] =
    IO.delay {
      val keyBytes = Base64.getDecoder.decode(masterKeyBase64)

      if (keyBytes.length != 32) {
        throw new IllegalArgumentException(
          "MASTER_KEY_BASE64 must decode to exactly 32 bytes"
        )
      }

      val secretKey = new SecretKeySpec(keyBytes, "AES")
      val secureRandom = new SecureRandom()

      new Crypto {

        private val algorithm = "AES/GCM/NoPadding"
        private val ivSizeBytes = 12
        private val tagSizeBits = 128

        override def encrypt(plainText: String): IO[String] =
          IO.blocking {
            val iv = new Array[Byte](ivSizeBytes)
            secureRandom.nextBytes(iv)

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(
              Cipher.ENCRYPT_MODE,
              secretKey,
              new GCMParameterSpec(tagSizeBits, iv)
            )

            val encryptedBytes =
              cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8))

            val payload = iv ++ encryptedBytes

            Base64.getEncoder.encodeToString(payload)
          }

        override def decrypt(cipherText: String): IO[String] =
          IO.blocking {
            val payload = Base64.getDecoder.decode(cipherText)

            if (payload.length <= ivSizeBytes) {
              throw new IllegalArgumentException("Invalid encrypted payload")
            }

            val iv = payload.take(ivSizeBytes)
            val encryptedBytes = payload.drop(ivSizeBytes)

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(
              Cipher.DECRYPT_MODE,
              secretKey,
              new GCMParameterSpec(tagSizeBits, iv)
            )

            val plainBytes = cipher.doFinal(encryptedBytes)

            new String(plainBytes, StandardCharsets.UTF_8)
          }
      }
    }
}