# Tarım Meteoroloji AI – Android

Konumdan 7 günlük meteoroloji verisi, MGM tarımsal hava tahmini bağlantısı, tarımsal risk analizi ve bitki hastalıkları kütüphanesi içeren başlangıç sürümü.

## APK
GitHub'a yükledikten sonra **Actions → Android APK → Run workflow** ile APK oluşturulur. Oluşan `TarimMeteorolojiAI-debug` artifact'i içinden APK indirilebilir.

## Veri kaynakları
- MGM: https://www.mgm.gov.tr/ ve Tarımsal Hava Tahmini: https://tarim.mgm.gov.tr/
- Open-Meteo: konum bazlı 7 günlük hava verisi
- Tarım ve Orman Bakanlığı TAGEM teknik talimatları: https://www.tarimorman.gov.tr/TAGEM/

## Önemli
Bitki koruma ürünü/doz bilgileri uygulamada sabit reçete olarak verilmemelidir. Ürün ve doz, Bakanlığın güncel Bitki Koruma Ürünleri Veri Tabanı ve ürün etiketi üzerinden doğrulanmalıdır. Uygulama karar destek amaçlıdır.
