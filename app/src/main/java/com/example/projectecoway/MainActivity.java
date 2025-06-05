package com.example.projectecoway;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private LatLng currentMarkerPosition;
    private String currentMarkerName;

    private ActivityResultLauncher<Intent> imageChooserLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Define o nome da permissão com base na versão do Android
    private final String imagePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa o launcher para seleção de imagem
        imageChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                // Converte a imagem URI em Bitmap
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                                // Redimensiona o Bitmap para um tamanho adequado para o ícone do marcador
                                int width = 100; // Largura desejada em pixels
                                int height = 100; // Altura desejada em pixels
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

                                // Cria o BitmapDescriptor a partir do Bitmap redimensionado
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

                                // Adiciona o marcador ao mapa
                                addMarkerToMap(currentMarkerPosition, currentMarkerName, icon);

                            } catch (IOException e) {
                                Log.e(TAG, "Erro ao carregar a imagem: ", e);
                                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Seleção de imagem cancelada", Toast.LENGTH_SHORT).show();
                    }
                });

        // Inicializa o launcher para solicitar permissão
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permissão concedida, abre o seletor de imagens
                        openImageChooserInternal();
                    } else {
                        // Permissão negada
                        Toast.makeText(this, "Permissão de acesso à galeria negada.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Obtém o SupportMapFragment e notifica quando o mapa estiver pronto
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Coordenadas de Fortaleza para visualização inicial
        LatLng fortaleza = new LatLng(-3.7319, -38.5267);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fortaleza, 12));

        // Define o listener para cliques no mapa
        mMap.setOnMapClickListener(this::showAddMarkerDialog);

        // Aqui você pode adicionar outras configurações do mapa, como:
        // mMap.setMyLocationEnabled(true); // Requer permissão de localização
        // mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    /**
     * Abre um diálogo para o usuário inserir o nome do marcador.
     * @param latLng A posição onde o usuário clicou no mapa.
     */
    private void showAddMarkerDialog(LatLng latLng) {
        currentMarkerPosition = latLng; // Armazena a posição atual

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nome do Marcador");

        // Configura o campo de entrada
        final EditText inputNome = new EditText(this);
        inputNome.setInputType(InputType.TYPE_CLASS_TEXT);
        inputNome.setHint("Digite o nome aqui");

        // Adiciona margens ao EditText para melhor aparência
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20); // Esquerda, Topo, Direita, Fundo
        inputNome.setLayoutParams(params);
        layout.addView(inputNome);
        builder.setView(layout);

        // Configura os botões do diálogo
        builder.setPositiveButton("OK", (dialog, which) -> handleMarkerNameInput(inputNome));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Valida o nome inserido e prossegue para a seleção do ícone.
     * @param inputNome O EditText contendo o nome inserido pelo usuário.
     */
    private void handleMarkerNameInput(EditText inputNome) {
        String nome = inputNome.getText().toString().trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome do marcador não pode ser vazio.", Toast.LENGTH_SHORT).show();
        } else {
            currentMarkerName = nome; // Armazena o nome
            showMarkerOptionsDialog(); // Mostra as opções de ícone
        }
    }

    /**
     * Mostra um diálogo com opções para escolher entre emoji ou imagem.
     */
    private void showMarkerOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha o Ícone do Marcador");

        String[] options = {"Usar emoji padrão", "Upload de imagem"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Opção "Usar emoji padrão"
                showEmojiSelectionDialog();
            } else {
                // Opção "Upload de imagem"
                openImageChooser();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Mostra um diálogo para selecionar um emoji padrão ou personalizar.
     */
    private void showEmojiSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um Emoji");

        final String[] emojis = {"🏠", "🏥", "🏫", "🛒", "🏞️", "✨ Personalizar"};
        builder.setItems(emojis, (dialog, which) -> {
            if (which == emojis.length - 1) {
                // Opção "Personalizar"
                showCustomEmojiDialog();
            } else {
                // Emoji padrão selecionado
                String selectedEmoji = emojis[which];
                BitmapDescriptor icon = createBitmapDescriptorFromText(selectedEmoji);
                if (icon != null) {
                    addMarkerToMap(currentMarkerPosition, currentMarkerName + " " + selectedEmoji, icon);
                } else {
                    Toast.makeText(this, "Erro ao criar ícone de emoji", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Mostra um diálogo para o usuário inserir um emoji personalizado.
     */
    private void showCustomEmojiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Digite um Emoji Personalizado");

        // Configura o campo de entrada para emoji
        final EditText inputEmoji = new EditText(this);
        inputEmoji.setInputType(InputType.TYPE_CLASS_TEXT); // Permite entrada de emoji
        inputEmoji.setHint("Digite o emoji aqui");

        // Adiciona margens ao EditText
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        inputEmoji.setLayoutParams(params);
        layout.addView(inputEmoji);
        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String emoji = inputEmoji.getText().toString().trim();
            if (!emoji.isEmpty()) {
                // Validação básica (poderia ser mais robusta para verificar se é realmente um emoji)
                BitmapDescriptor icon = createBitmapDescriptorFromText(emoji);
                if (icon != null) {
                    addMarkerToMap(currentMarkerPosition, currentMarkerName + " " + emoji, icon);
                } else {
                    Toast.makeText(this, "Erro ao criar ícone de emoji", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nenhum emoji inserido.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Verifica a permissão e inicia o seletor de imagens.
     */
    private void openImageChooser() {
        if (ContextCompat.checkSelfPermission(this, imagePermission) == PackageManager.PERMISSION_GRANTED) {
            // Permissão já concedida
            openImageChooserInternal();
        } else if (shouldShowRequestPermissionRationale(imagePermission)) {
            // Exibe um diálogo explicando por que a permissão é necessária
            new AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("Precisamos de acesso à sua galeria para que você possa selecionar uma imagem para o marcador.")
                    .setPositiveButton("OK", (dialog, which) -> requestPermissionLauncher.launch(imagePermission))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            // Solicita a permissão diretamente
            requestPermissionLauncher.launch(imagePermission);
        }
    }

    /**
     * Abre o seletor de imagens usando um Intent.
     */
    private void openImageChooserInternal() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imageChooserLauncher.launch(intent);
    }

    /**
     * Adiciona um marcador ao mapa na posição especificada com título e ícone.
     *
     * @param position Posição (LatLng) do marcador.
     * @param title    Título do marcador.
     * @param icon     Ícone (BitmapDescriptor) do marcador.
     */
    private void addMarkerToMap(LatLng position, String title, BitmapDescriptor icon) {
        if (mMap == null) {
            Log.e(TAG, "Mapa não inicializado ao tentar adicionar marcador.");
            Toast.makeText(this, "Erro: Mapa não está pronto.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (position == null) {
            Log.e(TAG, "Posição nula ao tentar adicionar marcador.");
            Toast.makeText(this, "Erro: Posição inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(icon));

        Log.i(TAG, "Marcador '" + title + "' adicionado em " + position.toString());
        Toast.makeText(this, "Marcador '" + title + "' adicionado!", Toast.LENGTH_SHORT).show();

        // Limpa as variáveis temporárias (opcional)
        // currentMarkerPosition = null;
        // currentMarkerName = null;
    }

    /**
     * Cria um BitmapDescriptor a partir de um texto (emoji).
     *
     * @param text O texto/emoji a ser convertido.
     * @return Um BitmapDescriptor ou null em caso de erro.
     */
    private BitmapDescriptor createBitmapDescriptorFromText(String text) {
        // Cria um TextView para renderizar o texto/emoji
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(30); // Ajuste o tamanho conforme necessário
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        // Mede o TextView
        textView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

        // Cria um Bitmap e desenha o TextView nele
        Bitmap bitmap = Bitmap.createBitmap(
                textView.getMeasuredWidth(),
                textView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        textView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

