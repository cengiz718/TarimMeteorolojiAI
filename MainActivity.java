package com.tarimmeteoroloji.ai;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout content, forecastRow, cropRow;
    private TextView locationText, analysisText, sourceText;
    private LocationManager locationManager;
    private double lat = 37.76, lon = 38.28;
    private final String[] crops = {"Buğday","Arpa","Nohut","Badem","Elma","Zeytin","Nar","Sebze","Bağ"};
    private final String[] colors = {"#2E7D32","#558B2F","#6D4C41","#795548","#C62828","#33691E","#AD1457","#00838F","#6A1B9A"};

    @Override public void onCreate(Bundle b) { super.onCreate(b); buildUI(); requestLocation(); fetchWeather(); }

    private TextView text(String s, float size, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.rgb(35,55,38));
        t.setPadding(18, 14, 18, 14); if (bold) t.setTypeface(null, 1); return t;
    }
    private GradientDrawable bg(String color, float radius) { GradientDrawable g=new GradientDrawable(); g.setColor(Color.parseColor(color)); g.setCornerRadius(radius); return g; }
    private LinearLayout card() { LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(16,12,16,12); c.setBackground(bg("#FFFFFF",22)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(14,8,14,8); c.setLayoutParams(p); return c; }

    private void buildUI() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(242,247,242));
        LinearLayout header=new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL); header.setPadding(20,18,20,18); header.setBackground(bg("#1B5E20",0));
        TextView title=text("🌾 TARIM AI",25,true); title.setTextColor(Color.WHITE); header.addView(title);
        TextView sub=text("Meteoroloji • Hastalık • İlaçlama • Tarımsal karar desteği",13,false); sub.setTextColor(Color.WHITE); header.addView(sub);
        root.addView(header,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout loc=card(); locationText=text("📍 Konum belirleniyor…",15,true); loc.addView(locationText); TextView refresh=text("Konumu ve tahmini yenile",14,false); refresh.setTextColor(Color.parseColor("#1B5E20")); refresh.setOnClickListener(v->{requestLocation();fetchWeather();}); loc.addView(refresh); content.addView(loc);

        LinearLayout ai=card(); TextView ah=text("🤖 YAPAY ZEKA TARIMSAL ANALİZ",18,true); ai.addView(ah); analysisText=text("7 günlük hava verisi bekleniyor…",15,false); ai.addView(analysisText); content.addView(ai);

        LinearLayout fc=card(); fc.addView(text("🌦️ 7 GÜNLÜK HAVA TAHMİNİ",18,true)); HorizontalScrollView hs=new HorizontalScrollView(this); forecastRow=new LinearLayout(this); forecastRow.setPadding(4,2,4,2); hs.addView(forecastRow); fc.addView(hs); content.addView(fc);

        LinearLayout cropsCard=card(); cropsCard.addView(text("🌱 ÜRÜN / HASTALIK REHBERİ",18,true)); TextView hint=text("Ürünü seçin: belirtiler, riskler ve mücadele planı",13,false); cropsCard.addView(hint); cropRow=new LinearLayout(this); cropRow.setOrientation(LinearLayout.VERTICAL); for(int i=0;i<crops.length;i++) addCrop(crops[i],i); cropsCard.addView(cropRow); content.addView(cropsCard);

        LinearLayout sources=card(); sources.addView(text("📚 RESMİ KAYNAKLAR",18,true)); addLink(sources,"☁ MGM Tarımsal Hava Tahmini","https://tarim.mgm.gov.tr/"); addLink(sources,"🧪 Bitki Koruma Ürünleri Veri Tabanı","https://bku.tarimorman.gov.tr/"); addLink(sources,"🌿 TAGEM / Zirai Mücadele Teknik Talimatları","https://www.tarimorman.gov.tr/TAGEM/"); sourceText=text("Not: İlaç adı, doz ve hasat aralığı yalnızca güncel ruhsat/etiket ve Bakanlık tavsiyesi doğrulanarak uygulanmalıdır.",12,false); sources.addView(sourceText); content.addView(sources);
        setContentView(root);
    }

    private void addLink(LinearLayout parent,String label,String url){ Button b=new Button(this); b.setText(label); b.setOnClickListener(v->open(url)); parent.addView(b); }
    private void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception ignored){}}

    private void requestLocation(){ locationManager=(LocationManager)getSystemService(LOCATION_SERVICE); if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},44);return;} getLocation(); }
    private void getLocation(){ try { Location best=null; for(String p:new String[]{LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER}){ if(locationManager.isProviderEnabled(p)){Location l=locationManager.getLastKnownLocation(p); if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}} if(best!=null){lat=best.getLatitude();lon=best.getLongitude();locationText.setText(String.format(Locale.US,"📍 Konum: %.4f, %.4f",lat,lon));} else locationText.setText("📍 Konum bulunamadı. GPS'yi açıp yenileyin."); } catch(Exception e){locationText.setText("📍 Konum izni alınamadı.");} }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==44){getLocation();fetchWeather();}}

    private void fetchWeather(){ new Thread(()->{ try{
        String u="https://api.open-meteo.com/v1/forecast?latitude="+lat+"&longitude="+lon+"&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,relative_humidity_2m_max&timezone=auto&forecast_days=7";
        HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection(); c.setConnectTimeout(12000); c.setReadTimeout(12000); c.setRequestMethod("GET");
        BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream())); StringBuilder sb=new StringBuilder(); String z; while((z=br.readLine())!=null)sb.append(z); c.disconnect();
        JSONObject o=new JSONObject(sb.toString()), d=o.getJSONObject("daily");
        JSONArray dates=d.getJSONArray("time"), codes=d.getJSONArray("weather_code"), hi=d.getJSONArray("temperature_2m_max"), lo=d.getJSONArray("temperature_2m_min"), rain=d.getJSONArray("precipitation_sum"), wind=d.getJSONArray("wind_speed_10m_max"), hum=d.getJSONArray("relative_humidity_2m_max");
        runOnUiThread(()->{renderForecast(dates,codes,hi,lo,rain,wind,hum); analysisText.setText(makeAI(dates,hi,lo,rain,wind,hum));});
      }catch(Exception e){runOnUiThread(()->{analysisText.setText("⚠️ Hava verisi alınamadı. İnternet bağlantısını kontrol edin.\n\nResmî MGM tarımsal tahminine aşağıdaki butondan ulaşabilirsiniz.");});} }).start(); }

    private void renderForecast(JSONArray dates,JSONArray codes,JSONArray hi,JSONArray lo,JSONArray rain,JSONArray wind,JSONArray hum){ forecastRow.removeAllViews(); try{for(int i=0;i<dates.length();i++){ LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setGravity(Gravity.CENTER); c.setPadding(14,10,14,10); c.setBackground(bg("#F1F8E9",18)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(145,-2); p.setMargins(5,3,5,3); c.setLayoutParams(p); String date=dates.getString(i).substring(5); c.addView(text(date,13,true)); c.addView(text(icon(codes.getInt(i))+" "+hi.getInt(i)+"° / "+lo.getInt(i)+"°C",15,true)); c.addView(text("💧 "+rain.getDouble(i)+" mm",12,false)); c.addView(text("💨 "+wind.getInt(i)+" km/s",12,false)); c.addView(text("Nem %"+hum.getInt(i),12,false)); forecastRow.addView(c);} }catch(Exception ignored){} }
    private String icon(int c){if(c==0)return"☀️";if(c<=3)return"⛅";if(c<60)return"🌫️";if(c<80)return"🌧️";if(c<90)return"🌨️";return"⛈️";}
    private String makeAI(JSONArray dates,JSONArray hi,JSONArray lo,JSONArray rain,JSONArray wind,JSONArray hum){ try{double total=0,maxWind=0;int wet=0,highHum=0,frost=0; for(int i=0;i<dates.length();i++){double r=rain.getDouble(i),w=wind.getDouble(i),h=hum.getDouble(i),l=lo.getDouble(i); total+=r;if(r>=2)wet++;if(w>25)maxWind=Math.max(maxWind,w);if(h>=80)highHum++;if(l<=2)frost++;} StringBuilder s=new StringBuilder("🤖 7 GÜNLÜK OTOMATİK ANALİZ\n"); if(total>=25)s.append("• Yağış yüksek (toplam ").append(String.format(Locale.US,"%.1f",total)).append(" mm): ilaçlama ve tarla girişini yağış sonrası planlayın.\n"); else if(total<5)s.append("• Yağış çok düşük: sulama ve kuraklık stresini takip edin.\n"); else s.append("• Yağış orta düzeyde: günlük tahmine göre işlem planlayın.\n"); if(highHum>=3)s.append("• Nem yüksek gün sayısı ").append(highHum).append(": mantari hastalık riski ürün bazında artabilir.\n"); if(maxWind>=25)s.append("• Rüzgâr ").append(String.format(Locale.US,"%.0f",maxWind)).append(" km/s'ye çıkıyor: püskürtmede sürüklenme riskine dikkat.\n"); if(frost>0)s.append("• Don riski: en düşük sıcaklık 2°C veya altında olan gün var. Hassas ürünleri koruyun.\n"); if(wet>=3&&highHum>=3)s.append("• Yağış + yüksek nem birlikte: buğday/arpa, nohut ve meyvelerde hastalık gözlemini sıklaştırın.\n"); s.append("• Bu analiz karar desteğidir; ilaçlama için güncel Bakanlık ruhsatı, etiket, fenoloji ve saha teşhisi esas alınmalıdır."); return s.toString(); }catch(Exception e){return"🤖 Analiz hazırlanamadı.";} }

    private void addCrop(String name,int idx){ Button b=new Button(this); b.setText("🌱 "+name); b.setTextSize(15); b.setTextColor(Color.WHITE); b.setBackground(bg(colors[idx%colors.length],18)); b.setOnClickListener(v->showCrop(name)); cropRow.addView(b); }
    private void showCrop(String crop){ String s=""; String title=crop+" • Hastalık ve mücadele"; if(crop.equals("Buğday")||crop.equals("Arpa")){s="Öne çıkan riskler: septorya/yaprak lekeleri, paslar, kök-kökboğazı hastalıkları.\n\nBelirtiler: yaprakta sarı-kahverengi lekeler, pas püstülleri, erken sararma ve kök boğazında çürüme.\n\nMücadele: temiz/sertifikalı tohum, münavebe, dengeli gübreleme, tarla hijyeni ve düzenli gözlem. Kimyasal mücadele ürün ve hastalık için güncel Bakanlık tavsiyesine göre.";} else if(crop.equals("Nohut")){s="Öne çıkan riskler: antraknoz ve kök/solgunluk sorunları.\n\nBelirtiler: yaprak-sürgünlerde koyu lekeler, kurumalar ve bitkide solgunluk.\n\nMücadele: temiz tohum, münavebe, hastalıklı bitkilerin uzaklaştırılması ve uygun tarla koşulları.";} else if(crop.equals("Zeytin")){s="Öne çıkan riskler: zeytin dal kanseri, halkalı leke ve zeytin sineği gibi zararlılar.\n\nBelirtiler ürüne göre yaprak lekeleri, dal yaraları ve meyvede zarar şeklinde görülür.\n\nMücadele: budama/hava sirkülasyonu, bahçe temizliği, izleme ve Bakanlık programına uygun zamanlama.";} else if(crop.equals("Badem")){s="Öne çıkan riskler: yaprak lekeleri, monilya ve dal/meyve hastalıkları.\n\nBelirti: yapraklarda lekelenme, sürgün kurumaları ve meyve çürüklüğü.\n\nMücadele: budama, bahçe hijyeni, hastalık döneminde düzenli gözlem ve ruhsatlı ürün etiketi.";} else if(crop.equals("Elma")){s="Öne çıkan riskler: karaleke, külleme ve ateş yanıklığı.\n\nBelirti: yaprakta lekeler, beyaz kül tabakası, sürgün/meyve kuruması.\n\nMücadele: hava sirkülasyonu, enfekteli materyalin uzaklaştırılması, fenoloji ve yağışa göre koruyucu program.";} else if(crop.equals("Nar")){s="Öne çıkan riskler: meyve çürüklükleri ve fungal hastalıklar.\n\nBelirti: meyvede yumuşama/çürüme, kabukta lekeler ve dal sorunları.\n\nMücadele: hijyen, hasarlı meyvelerin uzaklaştırılması, dengeli sulama ve güncel ruhsatlı tavsiye.";} else if(crop.equals("Sebze")){s="Ürüne göre mildiyö, külleme, bakteriyel hastalıklar ve kök çürüklükleri görülebilir.\n\nBelirti fotoğrafı olmadan kesin teşhis yapılmamalıdır. Yaprak altı, gövde ve kök birlikte incelenmelidir.";} else {s="Bağda külleme, mildiyö, kurşuni küf ve çeşitli zararlılar önemlidir.\n\nHastalık riski; sıcaklık, yaprak ıslaklığı, yağış ve asmanın gelişme dönemine göre değişir. Düzenli gözlem ve zamanında müdahale gerekir.";}
        new AlertDialog.Builder(this).setTitle("🌿 "+title).setMessage(s+"\n\n⚠️ İlaç adı/dozu/tarihi: Güncel Bitki Koruma Ürünleri Veri Tabanı ve ürün etiketi doğrulanmadan uygulanmamalıdır.").setPositiveButton("Bakanlık Veritabanı",(d,w)->open("https://bku.tarimorman.gov.tr/")).setNegativeButton("Kapat",null).show(); }
}
