package com.seuprojeto.bluetootharduino

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var btnConectar: Button
    private lateinit var btnLigar: Button
    private lateinit var btnDesligar: Button
    private lateinit var status: TextView

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private val deviceName = "HC-05" // Nome do seu módulo Bluetooth
    private val meuUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID padrão SPP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConectar = findViewById(R.id.btnConectar)
        btnLigar = findViewById(R.id.btnLigar)
        btnDesligar = findViewById(R.id.btnDesligar)
        status = findViewById(R.id.status)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não está disponível", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        btnConectar.setOnClickListener {
            conectarBluetooth()
        }

        btnLigar.setOnClickListener {
            enviarDados("1") // Envia '1' para ligar LED
        }

        btnDesligar.setOnClickListener {
            enviarDados("0") // Envia '0' para desligar LED
        }
    }

    private fun conectarBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        if (!pairedDevices.isNullOrEmpty()) {
            for (device in pairedDevices) {
                if (device.name == deviceName) {
                    bluetoothDevice = device
                    break
                }
            }
        }

        if (bluetoothDevice != null) {
            try {
                bluetoothSocket = bluetoothDevice
                    ?.createRfcommSocketToServiceRecord(meuUUID)
                bluetoothSocket?.connect()

                outputStream = bluetoothSocket?.outputStream
                inputStream = bluetoothSocket?.inputStream

                status.text = "Status: Conectado a $deviceName"
                Toast.makeText(this, "Conectado com sucesso!", Toast.LENGTH_SHORT).show()

            } catch (e: IOException) {
                e.printStackTrace()
                status.text = "Status: Falha na conexão"
                Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Dispositivo não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarDados(dados: String) {
        if (outputStream != null) {
            try {
                outputStream?.write(dados.toByteArray())
                Toast.makeText(this, "Enviado: $dados", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Erro ao enviar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth não conectado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            outputStream?.close()
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
