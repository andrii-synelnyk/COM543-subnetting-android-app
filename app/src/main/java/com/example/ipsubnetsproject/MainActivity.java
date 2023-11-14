package com.example.ipsubnetsproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText ipAddressInput;
    private EditText subnetMaskInput;
    private TextView resultTextView;
    private Button calculateButton;
    private Button aboutButton;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressInput = findViewById(R.id.ipAddressInput);
        subnetMaskInput = findViewById(R.id.subnetMaskInput);
        resultTextView = findViewById(R.id.resultTextView);
        calculateButton = findViewById(R.id.calculateButton);
        aboutButton = findViewById(R.id.aboutButton);
        helpButton = findViewById(R.id.helpButton);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = ipAddressInput.getText().toString();
                String subnetMask = subnetMaskInput.getText().toString();
                String result = calculate(ipAddress, subnetMask);
                resultTextView.setText(result);
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(helpIntent);
            }
        });
    }

    private String calculate(String ipAddress, String subnetMask) {

        if (!isValidIPAddress(ipAddress) || !isValidIPAddress(subnetMask)) {
            return "Invalid IP address or subnet mask.";
        }

        char ipClass = determineClass(ipAddress);
        String result;
        switch (ipClass) {
            case 'A':
                result = "The IPv4 address entered is Class A: \n\n" + calculateSubnetForClassA(ipAddress, subnetMask);
                break;
            case 'B':
                result = "The IPv4 address entered is Class B: \n\n" + calculateSubnetForClassB(ipAddress, subnetMask);
                break;
            case 'C':
                result = "The IPv4 address entered is Class C: \n\n" + calculateSubnetForClassC(ipAddress, subnetMask);
                break;
            default:
                result = "IP address class is not A, B, or C, or is invalid.";
                break;
        }
        return result;
    }

    private char determineClass(String ipAddress) {
        int firstOctet = Integer.parseInt(ipAddress.split("\\.")[0]);
        if (firstOctet >= 1 && firstOctet <= 127) return 'A';
        if (firstOctet >= 128 && firstOctet <= 191) return 'B';
        if (firstOctet >= 192 && firstOctet <= 223) return 'C';
        return 'D'; // Other classes are not handled in this app
    }

    private String calculateSubnetForClassC(String ipAddress, String subnetMask) {

        // Check if the IP address and subnet mask are valid
        if (!isValidIPAddress(ipAddress) || !isValidIPAddress(subnetMask)) {
            return "Invalid IP address or subnet mask.";
        }

        // Split IP address and subnet mask into octets
        String[] ipOctets = ipAddress.split("\\.");
        String[] maskOctets = subnetMask.split("\\.");

        // Convert subnet mask to binary string
        String binString = "";
        for (String octet : maskOctets) {
            String binary = Integer.toBinaryString(Integer.parseInt(octet));
            while (binary.length() < 8) {
                binary = "0" + binary;
            }
            binString += binary;
        }

        // Count the number of 1's in the binary string
        int count = 0;
        for (char bit : binString.toCharArray()) {
            if (bit == '1') {
                count++;
            }
        }

        int hostBits = 32 - count; // total number of bits in ipv4 is 32
        int hostsPerNetwork = (int) Math.pow(2, hostBits) - 2; // (2 to the power of n) - 2
        int extraBits = count - 24; // Class C default is 24 bits for the network
        int numberOfNetworks = (int) Math.pow(2, extraBits);

        // Calculate all subnets
        int octet3 = Integer.parseInt(ipOctets[2]);
        int octet4 = 0; // Start from the first possible host in the subnet

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfNetworks; i++) {
            // Network ID
            String networkID = ipOctets[0] + "." + ipOctets[1] + "." + octet3 + "." + octet4;

            // First usable IP
            octet4++;
            String firstUsableIP = ipOctets[0] + "." + ipOctets[1] + "." + octet3 + "." + octet4;

            // Last usable IP
            octet4 += hostsPerNetwork - 1;
            String lastUsableIP = ipOctets[0] + "." + ipOctets[1] + "." + octet3 + "." + octet4;

            // Broadcast IP
            octet4++;
            String broadcastIP = ipOctets[0] + "." + ipOctets[1] + "." + octet3 + "." + octet4;

            // Append to result
            result.append("Subnet #").append(i+1)
                    .append("\nNetwork ID: ").append(networkID)
                    .append("\nUsable Range: ").append(firstUsableIP).append(" - ").append(lastUsableIP)
                    .append("\nBroadcast IP: ").append(broadcastIP)
                    .append("\n\n");

            // Prepare octet4 for the next network
            octet4++;
        }

        return result.toString();
    }

    private String calculateSubnetForClassB(String ipAddress, String subnetMask) {
        return "";
    }

    private String calculateSubnetForClassA(String ipAddress, String subnetMask) {
        return "";
    }

    private boolean isValidIPAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int intPart = Integer.parseInt(part);
                if (intPart < 0 || intPart > 255) {
                    return false;
                }
            } catch (NumberFormatException nfe) {
                return false;
            }
        }

        return true;
    }


}
