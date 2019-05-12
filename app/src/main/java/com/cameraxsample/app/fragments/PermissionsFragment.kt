package com.cameraxsample.app.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import androidx.navigation.NavOptions
import androidx.navigation.Navigation

import com.cameraxsample.app.R


private const val PERMISSION_REQUEST_CODE = 2222
private val PERMISSION_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

class PermissionsFragment : Fragment() {

    val navigationOptions = NavOptions.Builder().setPopUpTo(R.id.permissionsFragment, true).build()

    /**
     * Burada uygulama içerisinde kullanmak istediğimiz izin durumlarını kontrol ediyoruz.
     * permissions.CAMERA, permission.RECOR_AUDIO, permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE
     * Almak istediğimiz tüm izinler verildiyse artık cameraFragmentimize dönebiliriz.
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            requestPermissions(PERMISSION_REQUIRED, PERMISSION_REQUEST_CODE)
        } else {
            //izin zaten verildiyse
            Navigation.findNavController(requireActivity(), R.id.fragmentContainer)
                .navigate(R.id.action_permissions_to_camera, null, navigationOptions)
        }
    }

    /**
     * Tüm izinlerin sağlandığı durumda true, sağlanamadığı durumda false döner
     */
    private fun hasPermissions(): Boolean {
        for (permission in PERMISSION_REQUIRED) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) !=
                PackageManager.PERMISSION_GRANTED
            )
                return false
        }
        return true
    }

    /**
     * Gerekli izinler sağlandığında camera fragmente yönlendirir.
     * İzinler sağlanamadığında Toast mesajı bastırır.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "İzinler alındı.", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireActivity(), R.id.fragmentContainer)
                    .navigate(R.id.action_permissions_to_camera, null, navigationOptions)
            } else {
                Toast.makeText(context, "İzin verilmedi.", Toast.LENGTH_LONG).show()
            }
        }

    }
}
