package cr.ac.tec.aa.proyecto_1_aa;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout relativeLayout;

    private Toolbar toolbar_1; // Barra de arriba
    private Toolbar toolbar_2; // Barra de abajo

    private SeekBar seekBar_1; // Selector de intensidad del filtro Gauss

    private HorizontalScrollView horizontal_Scroll_View;

    private Button button_Filter_Averaging;
    private Button button_Filter_Desaturation;
    private Button button_Filter_Decomposition_Max;
    private Button button_Filter_Decomposition_Min;
    private Button button_Filter_GaussianBlur;
    private Button button_Filter_MercaPropia;

    private ImageView imageView_Preview; // Cuadro de vista previa de la imagen/foto

    private MenuItem buttonMenu_ImageInfo;
    private MenuItem buttonMenu_SaveImage;
    private MenuItem buttonMenu_ShowOriginalImage;

    private Bitmap originalImage;
    private Bitmap modifiedImage;
    private Bitmap modifiedImage_Averaging = null;
    private Bitmap modifiedImage_Desaturation = null;
    private Bitmap modifiedImage_DecompositionMax = null;
    private Bitmap modifiedImage_DecompositionMin = null;
    private Bitmap modifiedImage_GaussianBlur = null;
    private Bitmap modifiedImage_MercaPropia = null;

    private ProgressDialog dialog;

    private boolean showComponents = true;

    private long filterTiming = 0;

    private int IMAGE_GALLERY_REQUEST = 1;
    public static final int REQUEST_CAPTURE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    public enum imageFilters{Averaging, Desaturation, Decomposition_Max, Decomposition_Min, GaussianBlur, MercaPropia};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRelativeLayout();
        initToolBar();
        initSeekBar();
        initHorizontalScrollView();
        initButtons();
        initImageViews();

        CheckPermissions();
    }


    //---------------------------- INICIALIZADORES ----------------------------//

    private void initRelativeLayout()
    {
        relativeLayout = (RelativeLayout)findViewById(R.id.RL_ID);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if(showComponents){HideComponents();}
                    else{ShowComponents();}
                    showComponents = !showComponents;
                }
                return true;
            }
        });
    }

    private void initToolBar()
    {
        toolbar_1 = (Toolbar)findViewById(R.id.toolbar_1);
        setSupportActionBar(toolbar_1);
    }

    private void initSeekBar()
    {
        seekBar_1 = (SeekBar)findViewById(R.id.gaussBar_ID);
        seekBar_1.setVisibility(View.INVISIBLE);

        SeekBar.OnSeekBarChangeListener listenerGaussBar = new SeekBar.OnSeekBarChangeListener() { // Es una interfaz

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                int barIntensity = seekBar_1.getProgress();

                switch (barIntensity)
                {
                    case 0:
                        CallCompute_GaussBlur(3);
                        break;

                    case 1:
                        CallCompute_GaussBlur(5);
                        break;

                    case 2:
                        CallCompute_GaussBlur(10);
                        break;

                    case 3:
                        CallCompute_GaussBlur(15);
                        break;

                    case 4:
                        CallCompute_GaussBlur(20);
                        break;

                    case 5:
                        CallCompute_GaussBlur(25);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
            }
        };
        seekBar_1.setOnSeekBarChangeListener(listenerGaussBar);
    }

    private void initHorizontalScrollView()
    {
        horizontal_Scroll_View = (HorizontalScrollView) findViewById(R.id.HorizontalScrollView_ID);
        horizontal_Scroll_View.setVisibility(View.INVISIBLE);
    }

    private void initButtons()
    {
        button_Filter_Averaging = (Button) findViewById(R.id.button_Averaging_ID);
        button_Filter_Averaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_GrayScale(imageFilters.Averaging);
                filterTiming = mierda.GetTime();
            }
        });

        button_Filter_Desaturation = (Button) findViewById(R.id.button_Desaturation_ID);
        button_Filter_Desaturation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_GrayScale(imageFilters.Desaturation); filterTiming = mierda.GetTime();
            }
        });

        button_Filter_Decomposition_Max = (Button) findViewById(R.id.button_Decomposition_Max_ID);
        button_Filter_Decomposition_Max.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_GrayScale(imageFilters.Decomposition_Max); filterTiming = mierda.GetTime();
            }
        });

        button_Filter_Decomposition_Min = (Button) findViewById(R.id.button_Decomposition_Min_ID);
        button_Filter_Decomposition_Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_GrayScale(imageFilters.Decomposition_Min); filterTiming = mierda.GetTime();
            }
        });

        button_Filter_GaussianBlur = (Button) findViewById(R.id.button_GaussianBlur_ID);
        button_Filter_GaussianBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_GaussBlur(10); filterTiming = mierda.GetTime();

            }
        });

        button_Filter_MercaPropia = (Button) findViewById(R.id.button_MercaPropia_ID);
        button_Filter_MercaPropia.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Chronometer mierda = new Chronometer();
                mierda.InitTime();
                CallCompute_MercaPropia(); filterTiming = mierda.GetTime();
            }
        });

    }

    private void initImageViews()
    {
        // Asignar el componente visual a la variable
        imageView_Preview = (ImageView) findViewById(R.id.imageView_vistaPrevia);
    }


    //---------------------------- FUNCIONALIDADES ----------------------------//

    private boolean initActivityMenu(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_Camera:
                LaunchCamera();
                break;

            case R.id.action_Gallery:
                OpenGallery();
                break;

            case R.id.action_Save:
                if(imageView_Preview.getDrawable() != null) { // Verificar si ya se ha cargado una imagen
                    ShowAlertDialogToSaveModifiedImage();
                }
                else{
                    ShowAlertMessage("¡Aplica un filtro a una imagen primero!");
                }
                break;

            case R.id.action_ShowOriginalImage:
                seekBar_1.setVisibility(View.INVISIBLE);
                SetImageView_Preview(originalImage);
                buttonMenu_ShowOriginalImage.setVisible(false);
                break;

            case R.id.action_ImageInfo:
                ShowAlertDialog("Imagen Actual", "Anchura: " + originalImage.getWidth() + "\n" + "Altura: " + originalImage.getHeight() + "\n" +
                        Chronometer.ToString(filterTiming));
                break;

            case R.id.action_InformationInstAA:
                ShowAlertDialog("Información", "Instituto Tecnológico de Costa Rica\nAnálisis de Algoritmos\nJosé Navarro Acuña\nJosué Suárez Campos\n2017");

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void OpenGallery()
    {
        // invoke the image gallery using an implict intent.
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    private void LaunchCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAPTURE); // --> public static final int REQUEST_CAPTURE = 1;
    }

    private void SaveModifiedImageIntoGallery()
    {
        SaveImage save = new SaveImage();
        save.SaveImage(getApplicationContext(), modifiedImage);
    }

    private void SetImagesModifiedNull()
    {
        modifiedImage_Averaging = null;
        modifiedImage_Desaturation = null;
        modifiedImage_DecompositionMax = null;
        modifiedImage_DecompositionMin = null;
        modifiedImage_GaussianBlur = null;
        modifiedImage_MercaPropia = null;
    }

    public void CallCompute_GrayScale(imageFilters pImageFilter)
    {
        boolean aplicarFiltro = true;
        if(pImageFilter.equals(imageFilters.Averaging) && modifiedImage_Averaging != null)
        {
            modifiedImage = modifiedImage_Averaging; aplicarFiltro = false;
        }
        if(pImageFilter.equals(imageFilters.Desaturation) && modifiedImage_Desaturation != null)
        {
            modifiedImage = modifiedImage_Desaturation; aplicarFiltro = false;
        }
        if(pImageFilter.equals(imageFilters.Decomposition_Max) && modifiedImage_DecompositionMax != null)
        {
            modifiedImage = modifiedImage_DecompositionMax; aplicarFiltro = false;
        }
        if(pImageFilter.equals(imageFilters.Decomposition_Min) && modifiedImage_DecompositionMin != null)
        {
            modifiedImage = modifiedImage_DecompositionMin; aplicarFiltro = false;
        }

        if(aplicarFiltro)
        {
            Convolution pConvolution = new Convolution();
            modifiedImage = pConvolution.Compute_GrayScale(originalImage, pImageFilter); // Ejecución

            if(pImageFilter.equals(imageFilters.Averaging)) {modifiedImage_Averaging = modifiedImage;}
            if(pImageFilter.equals(imageFilters.Desaturation)) {modifiedImage_Desaturation = modifiedImage;}
            if(pImageFilter.equals(imageFilters.Decomposition_Max)) {modifiedImage_DecompositionMax = modifiedImage;}
            if(pImageFilter.equals(imageFilters.Decomposition_Min)) {modifiedImage_DecompositionMin = modifiedImage;}
            if(pImageFilter.equals(imageFilters.GaussianBlur)) {modifiedImage_GaussianBlur = modifiedImage;}
            if(pImageFilter.equals(imageFilters.MercaPropia)) {modifiedImage_MercaPropia = modifiedImage;}
        }
        SetAfterApplyFilter();
    }

    public void CallCompute_GaussBlur(int pRadio)
    {
        boolean aplicarFiltro = true;
        //if(modifiedImage_GaussianBlur != null)

            //modifiedImage = modifiedImage_GaussianBlur; aplicarFiltro = false;


        //if(aplicarFiltro)

            Convolution pConvolution = new Convolution();
            modifiedImage = pConvolution.Compute_GaussianBlur(originalImage, 0.5f, pRadio);
            //pConvolution.ComputeConvolution_NormalScale(originalImage, pImageFilter); // Ejecución

            modifiedImage_GaussianBlur = modifiedImage;

            seekBar_1.setVisibility(View.VISIBLE);


        SetImageView_Preview(modifiedImage);
        buttonMenu_ShowOriginalImage.setVisible(true);
        buttonMenu_SaveImage.setVisible(true);
        ShowAlertMessage("¡Filtro aplicado!");
    }

    public void CallCompute_MercaPropia()
    {
        boolean aplicarFiltro = true;
        if(modifiedImage_MercaPropia != null)
        {
            modifiedImage = modifiedImage_MercaPropia; aplicarFiltro = false;
        }
        if(aplicarFiltro)
        {
            Convolution pConvolution = new Convolution();
            modifiedImage = pConvolution.ComputeConvolution_MercaPropia(originalImage); // Ejecución

            modifiedImage_MercaPropia = modifiedImage;

            seekBar_1.setVisibility(View.INVISIBLE);
        }
        SetAfterApplyFilter();
    }

    //---------------------------- UTILIDADES ----------------------------//

    private void ShowAlertMessage(String pMessage)
    {
        Toast.makeText(getApplicationContext(), pMessage, Toast.LENGTH_SHORT).show();
    }

    private void ShowAlertDialog(String pTittle, String pMessage)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(pTittle);
        alertDialog.setMessage(pMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CERRAR",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void ShowAlertDialogToSaveModifiedImage()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("¿Deseas guardar la imagen?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SaveModifiedImageIntoGallery();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void ShowLoadingMessage() // En proceso ...
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog = ProgressDialog.show(MainActivity.this, "",
                                "Loading. Please wait...", true);
                    }
                });
            }
        }).start();
    }

    private void SetImageView_Preview(Bitmap img)
    {
        imageView_Preview.setImageBitmap(img);
    }

    private void HideComponents()
    {
        toolbar_1.animate().translationY(-toolbar_1.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        toolbar_2.animate().translationY(toolbar_2.getTop()).setInterpolator(new AccelerateInterpolator()).start();

        horizontal_Scroll_View.animate().translationY(horizontal_Scroll_View.getTop()).setInterpolator(new AccelerateInterpolator()).start();

        seekBar_1.animate().translationY(seekBar_1.getTop()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void ShowComponents()
    {
        toolbar_1.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        toolbar_2.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();

        horizontal_Scroll_View.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();

        seekBar_1.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void SetAfterApplyFilter()
    {
        seekBar_1.setVisibility(View.INVISIBLE);
        SetImageView_Preview(modifiedImage);
        buttonMenu_ShowOriginalImage.setVisible(true);
        buttonMenu_SaveImage.setVisible(true);
        seekBar_1.setProgress(2);
        ShowAlertMessage("¡Filtro aplicado!");
    }


    //---------------------------- on...() ----------------------------//

        @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Linkear el toolbar 1 (contiene un menu) con el archivo menu_1.xml
        getMenuInflater().inflate(R.menu.menu_1, menu);

        toolbar_2 = (Toolbar) findViewById(R.id.toolbar_2);
        toolbar_2.inflateMenu(R.menu.menu_2);//changed
        //toolbar2 menu items CallBack listener
        toolbar_2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) { // Para toolbar 2

                if(initActivityMenu(item)) {return true;}
                return false;
            }
        });
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        buttonMenu_ImageInfo = menu.findItem(R.id.action_ImageInfo);
        buttonMenu_SaveImage = menu.findItem(R.id.action_Save);
        buttonMenu_ShowOriginalImage = menu.findItem(R.id.action_ShowOriginalImage);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    { // Para toolbar_1
        if(initActivityMenu(item)) {return true;}
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            // if we are here, everything processed successfully.
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // if we are here, we are hearing back from the image gallery.

                // the address of the image on the SD Card.
                Uri imageUri = data.getData();

                // declare a stream to read the image data from the SD Card.
                InputStream inputStream;

                // we are getting an input stream, based on the URI of the image.
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // get a bitmap from the stream.
                    originalImage = BitmapFactory.decodeStream(inputStream);

                    // show the image to the user
                    SetImageView_Preview(originalImage);

                    // show the horizontal scroll to the view
                    horizontal_Scroll_View.setVisibility(View.VISIBLE);

                    buttonMenu_SaveImage.setVisible(false);
                    buttonMenu_ImageInfo.setVisible(true);
                    buttonMenu_ShowOriginalImage.setVisible(false);
                    seekBar_1.setVisibility(View.INVISIBLE);
                    filterTiming = 0;
                    seekBar_1.setProgress(2);

                    SetImagesModifiedNull();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // show a message to the user indictating that the image is unavailable.
                    Toast.makeText(this, "Imagen con formato inválido", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override // Se ejecuta una sola vez cuando se concede el permiso
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permisos de camara aceptados", Toast.LENGTH_LONG).show();
            }
            else
                {
                Toast.makeText(this, "Permisos de camara denegados", Toast.LENGTH_LONG).show();
            }
        }
    }


    //---------------------------- PERMISOS ----------------------------//

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckPermissions()
    {
        // Camara
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }
        // Guardar
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_CAMERA_REQUEST_CODE);
        }
    }

}
