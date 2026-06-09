# Modernisasi UI & Desain Responsif — SQLite-Gen

**Tanggal:** 2026-06-09
**Status:** Disetujui (menunggu review spec)
**Pendekatan:** A — Master-detail penuh dengan Material 3 Adaptive

## Tujuan

Meningkatkan kualitas aplikasi SQLite-Gen dari sisi tampilan (visual modern & profesional)
dan responsivitas (mendukung phone, tablet, dan landscape), tanpa mengubah logika data,
generator skrip, import/export, atau fitur AI.

## Lingkup

- **Termasuk:** lapisan UI Jetpack Compose (theme, komponen, layout), layout adaptif
  master-detail, polish keempat layar.
- **Tidak termasuk:** Room/DAO, `SchemaViewModel` (kecuali helper kecil non-perilaku),
  `SchemaGenerator`, `AiColumnGenerator`, `SchemaRepository`, model data.

## Keadaan saat ini

- 4 layar: `HomeScreen`, `TableDetailScreen`, `CodePreviewScreen`, `ImportExportScreen`.
- Tema hijau Material 3 default (`Color.kt`), tipografi default (`Type.kt` hanya `bodyLarge`).
- Navigasi `NavHost` string sederhana: `home`, `table/{tableId}`, `code`, `import_export`.
- `enableEdgeToEdge()` aktif. `dynamicColor = false`.
- Compose BOM `2024.09.00` (Material3 ~1.3 stabil). Belum ada library adaptive.

## Keputusan desain

### 1. Fondasi tema (design tokens)

**`Color.kt`** — palet baru:
- Primary: indigo/biru-keunguan; aksen sekunder/tersier: teal.
- Skema light + dark lengkap (semua peran Material 3 color scheme).
- Warna semantik per tipe data SQLite untuk chip:
  - INTEGER, TEXT, REAL, BLOB, NUMERIC — masing-masing warna container + onContainer
    yang konsisten di light & dark.
- Disediakan sebagai map/helper agar bisa dipakai komponen chip tipe.

**`Type.kt`** — skala tipografi penuh:
- Definisikan display/headline/title/body/label dengan ukuran, lineHeight, letterSpacing,
  dan FontWeight yang dirapikan. `FontFamily.Default` (tanpa menambah font kustom agar
  build tetap ringan).

**`Shape.kt`** (baru):
- `Shapes` Material 3 dengan corner lebih membulat (mis. small 12dp, medium 16dp,
  large 24dp).

**`Theme.kt`**:
- Daftarkan `shapes = Shapes` pada `MaterialTheme`.
- `dynamicColor` tetap default `false`.
- Pastikan status bar / navigation bar transparan mengikuti edge-to-edge.

### 2. Polish komponen per layar

**Home (daftar tabel):**
- Kartu tabel: ikon tabel di kiri, nama (titleMedium bold), baris chip jumlah kolom +
  badge "PK" bila ada primary key. Elevasi/tonal surface menggantikan border datar.
- Empty state: ikon + judul + deskripsi + tombol CTA "Tambah Tabel" (bukan teks polos).
- Dialog konfirmasi sebelum menghapus tabel.
- Dialog "New Table" dipoles (judul, field, tombol konsisten).

**Table Detail (editor kolom):**
- `ColumnEditorCard` didesain ulang:
  - Field nama kolom + tombol hapus di header kartu.
  - Dropdown tipe data menampilkan warna chip sesuai tipe.
  - Flag PK / AutoInc / NotNull / Unique sebagai `FilterChip` yang dapat di-tap
    (menggantikan deretan `Checkbox` sempit). Tetap memanggil `onUpdate(column.copy(...))`.
  - Field Default Value & Foreign Key tetap, spacing lebih lega.
- Tombol AI auto-generate: state loading lebih jelas (indikator + disable saat proses).
- Dialog edit nama tabel dipoles.

**Code Preview:**
- Tab bahasa (`better-sqlite3`, `node:sqlite`, `sqlite3`, `raw_sql`) di-styling.
- Area kode: latar gelap konsisten, teks monospace (`FontFamily.Monospace`),
  scroll vertikal + horizontal bila perlu, `SelectionContainer` tetap.
- Tombol copy dipertahankan; opsional tombol share via `Intent` (boleh diskip bila
  menambah kompleksitas).

**Import/Export:**
- Tiap grup (Preview Data / JSON / SQL) menjadi `Card` bersection dengan ikon & judul.
- Tombol jadi grid adaptif. Output preview field tetap read-only dengan tombol copy.

**Motion:**
- Transisi navigasi halus (slide horizontal + fade) untuk rute non-adaptive.

### 3. Responsivitas — Master-Detail (Pendekatan A)

**Dependency baru** (ditambah ke `gradle/libs.versions.toml` + `app/build.gradle.kts`):
- `androidx.compose.material3.adaptive:adaptive`
- `androidx.compose.material3.adaptive:adaptive-layout`
- `androidx.compose.material3.adaptive:adaptive-navigation`
- `androidx.compose.material3:material3-window-size-class`
- Gunakan versi stabil terbaru yang kompatibel dengan Compose BOM 2024.09 (lini 1.0.x).
  Versi final diverifikasi saat implementasi via build.

**Layout:**
- Home + TableDetail digabung menjadi satu komposisi berbasis
  `ListDetailPaneScaffold` + `rememberListDetailPaneScaffoldNavigator`:
  - **Compact (phone)**: satu pane; daftar → tap → detail (perilaku seperti sekarang,
    termasuk tombol back).
  - **Medium/Expanded (tablet/landscape)**: daftar di pane kiri, editor kolom di
    pane kanan, tampil bersamaan.
- Editor kolom pada lebar medium/expanded: kartu kolom memakai `LazyVerticalGrid`
  adaptif (≥2 kolom), pada compact tetap 1 kolom (`LazyColumn`).
- `CodePreviewScreen` & `ImportExportScreen`: batasi lebar konten maksimum (~840dp)
  dan center pada layar lebar; tombol import/export jadi grid adaptif.

**Navigasi (`AppNavigation.kt`):**
- Rute `home` + `table/{tableId}` diganti satu rute `list_detail` yang memuat scaffold
  adaptive. Rute `code` & `import_export` tetap `composable` terpisah.
- State pemilihan tabel dikelola oleh navigator adaptive (selected tableId).

### 4. Perubahan ViewModel (minimal, non-perilaku)

- Boleh menambah helper read-only bila perlu (mis. `fun tableById(id): TableWithColumns?`
  yang membaca `allTables.value`). Tidak mengubah perilaku/logika yang ada.

## Penanganan error & edge case

- Tabel yang dipilih lalu dihapus: detail pane kembali ke empty state (bukan crash).
  Saat ini `TableDetailScreen` memanggil `onBack()` bila tabel null — perilaku setara
  dipertahankan di scaffold (tampilkan placeholder "Pilih tabel").
- Daftar kosong: empty state di list pane; detail pane menampilkan placeholder.
- Edge-to-edge: semua layar menghormati `Scaffold` padding + insets.

## Testing & verifikasi

- `./gradlew assembleDebug` harus sukses.
- Test unit/Robolectric yang ada tetap lulus.
- Screenshot test Roborazzi (`app/src/test/screenshots/greeting.png`): bila komponen
  terkait berubah, baseline diperbarui (`./gradlew recordRoborazziDebug` atau setara).
- Verifikasi manual responsif: jalankan di emulator phone + tablet/landscape untuk
  memastikan master-detail bekerja.

## Risiko

- Kompatibilitas versi library adaptive dengan Compose BOM 2024.09 — dimitigasi dengan
  memverifikasi build saat implementasi; bila bentrok, naikkan BOM atau pin versi
  adaptive yang cocok.
- Refactor navigasi master-detail menyentuh `AppNavigation`, `HomeScreen`,
  `TableDetailScreen` — dikerjakan bertahap dengan build hijau di tiap langkah.

## Urutan implementasi (ringkas)

1. Design tokens: `Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`.
2. Polish komponen tiap layar (tanpa ubah navigasi dulu).
3. Tambah dependency adaptive + window-size-class.
4. Refactor master-detail (`AppNavigation` + Home/TableDetail).
5. Batas lebar konten + grid adaptif untuk Code Preview & Import/Export.
6. Build, perbaiki baseline screenshot bila perlu, verifikasi manual.
