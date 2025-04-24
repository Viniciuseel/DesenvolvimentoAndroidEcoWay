package com.example.gmaps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.AlertDialog;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int EMOJI_SIZE = 64;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final LatLng DEFAULT_LOCATION = new LatLng(-3.7319, -38.5267);
    private static final int DEFAULT_ZOOM = 12;

    private GoogleMap mMap;
    private LatLng currentMarkerPosition;
    private String currentMarkerName;
    private Uri currentImageUri;

    private FloatingActionButton fabAddMarker;
    private CardView imageDialogContainer;
    private ImageView fullSizeImage;
    private Button btnCloseDialog;
    private TextView dialogTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa views
        fabAddMarker = findViewById(R.id.fab_add_marker);
        imageDialogContainer = findViewById(R.id.image_dialog_container);
        fullSizeImage = findViewById(R.id.full_size_image);
        btnCloseDialog = findViewById(R.id.btn_close_dialog);
        dialogTitle = findViewById(R.id.dialog_title);

        // Configura listeners
        fabAddMarker.setOnClickListener(v -> {
            if (mMap != null) {
                LatLng center = mMap.getCameraPosition().target;
                showAddMarkerDialog(center);
            }
        });

        btnCloseDialog.setOnClickListener(v -> imageDialogContainer.setVisibility(View.GONE));

        initializeMap();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        setupMapListeners();
    }

    private void setupMap() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(DEFAULT_LOCATION)
                .zoom(DEFAULT_ZOOM)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnMarkerClickListener(this);
    }

    private void setupMapListeners() {
        mMap.setOnMapClickListener(this::showAddMarkerDialog);
    }

    private void showAddMarkerDialog(LatLng latLng) {
        currentMarkerPosition = latLng;
        final EditText inputNome = new EditText(this);
        inputNome.setHint("Nome do local");

        new AlertDialog.Builder(this)
                .setTitle("Novo marcador")
                .setMessage("Digite o nome para este ponto:")
                .setView(inputNome)
                .setPositiveButton("Avançar", (dialog, which) -> handleMarkerNameInput(inputNome))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void handleMarkerNameInput(EditText inputNome) {
        currentMarkerName = inputNome.getText().toString().trim();
        if (currentMarkerName.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um nome válido", Toast.LENGTH_SHORT).show();
            return;
        }
        showMarkerOptionsDialog();
    }

    private void showMarkerOptionsDialog() {
        String[] options = {
                "Usar emoji padrão",
                "Upload de imagem do mapa interno"
        };

        new AlertDialog.Builder(this)
                .setTitle("Opções do marcador")
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        showEmojiSelectionDialog();
                    } else {
                        openImageChooser();
                    }
                })
                .show();
    }

    private void showEmojiSelectionDialog() {
        String[] emojis = {
                "🏠 Casa", "🏥 Hospital", "🏫 Escola",
                "🛒 Mercado", "🏞️ Parque", "✨ Personalizar"
        };

        new AlertDialog.Builder(this)
                .setTitle("Escolha um emoji")
                .setItems(emojis, (dialogInterface, i) -> {
                    if (emojis[i].contains("Personalizar")) {
                        showCustomEmojiDialog();
                    } else {
                        String emoji = emojis[i].split(" ")[0];
                        addMarkerToMap(emoji, null);
                    }
                })
                .show();
    }

    private void showCustomEmojiDialog() {
        final EditText inputEmoji = new EditText(this);
        inputEmoji.setHint("Digite um emoji (ex: 🐶, 🚀)");

        new AlertDialog.Builder(this)
                .setTitle("Emoji personalizado")
                .setMessage("Escolha seu próprio emoji:")
                .setView(inputEmoji)
                .setPositiveButton("OK", (dlg, w) -> {
                    String emojiCustom = inputEmoji.getText().toString().trim();
                    if (!emojiCustom.isEmpty()) {
                        addMarkerToMap(emojiCustom, null);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            currentImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentImageUri);
                addMarkerToMap("🏢", bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addMarkerToMap(String emoji, Bitmap customImage) {
        Bitmap markerIcon;

        if (customImage != null) {
            markerIcon = Bitmap.createScaledBitmap(customImage, 100, 100, false);
        } else {
            markerIcon = createEmojiBitmap(emoji);
        }

        mMap.addMarker(new MarkerOptions()
                .position(currentMarkerPosition)
                .title(currentMarkerName)
                .snippet(currentImageUri != null ? currentImageUri.toString() : null)
                .icon(BitmapDescriptorFactory.fromBitmap(markerIcon)));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String imageUriString = marker.getSnippet();

        if (imageUriString != null && !imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);
            showImageDialog(imageUri, marker.getTitle());
            return true;
        }
        return false;
    }

    private void showImageDialog(Uri imageUri, String title) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            dialogTitle.setText(title);
            fullSizeImage.setImageBitmap(bitmap);
            imageDialogContainer.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao exibir imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap createEmojiBitmap(String emoji) {
        Paint paint = new Paint();
        paint.setTextSize(EMOJI_SIZE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Rect bounds = new Rect();
        paint.getTextBounds(emoji, 0, emoji.length(), bounds);

        Bitmap bitmap = Bitmap.createBitmap(
                bounds.width(),
                bounds.height(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(emoji, 0, bounds.height(), paint);

        return bitmap;
    }
}