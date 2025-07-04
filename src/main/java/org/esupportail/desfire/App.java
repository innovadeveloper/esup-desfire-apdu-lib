package org.esupportail.desfire;

import org.esupportail.desfire.service.DESFireApduService;
import org.esupportail.desfire.service.DESFireCompleteService;
import org.esupportail.desfire.service.PcscUsbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private PcscUsbService pcscUsbService = new PcscUsbService();

    public static void main(String[] args) {
        new App().run();
    }

    public void run(){
        DESFireApduService service = new DESFireApduService();
        DESFireCompleteService completeService = new DESFireCompleteService();

        log.info("Starting DESFire demonstration...");

        try {
            // Connect to card reader
            String cardTerminalName = pcscUsbService.connection();
            log.info("Connected to card terminal: " + cardTerminalName);
            
            // Connect complete service to card
            if (!completeService.connect()) {
                log.error("Failed to connect to card");
                log.info("Please place a DESFire card on one of the readers and try again");
                return;
            }
            
            log.info("Card connected successfully");
            
            // Get and display complete version information
            log.info("Getting card version information...");
            String versionInfo = completeService.getVersionComplete();
            System.out.println("\n" + versionInfo);
            
            // Get free memory
            log.info("Getting free memory...");
            int freeMemory = completeService.getFreeMemory();
            if (freeMemory >= 0) {
                System.out.println("Free memory: " + freeMemory + " bytes");
            } else {
                System.out.println("Failed to get free memory");
            }
            
            // Get application IDs
            log.info("Getting application IDs...");
            byte[] appIds = completeService.getApplicationIds();
            if (appIds != null && appIds.length > 0) {
                System.out.println("Applications found: " + appIds.length / 3);
                for (int i = 0; i < appIds.length; i += 3) {
                    System.out.printf("  App %d: %02X%02X%02X%n", 
                        (i/3) + 1, appIds[i], appIds[i+1], appIds[i+2]);
                }
            } else {
                System.out.println("No applications found or error reading applications");
            }
            
            // Select root application
            log.info("Selecting root application...");
            byte[] rootAid = {0x00, 0x00, 0x00};
            if (completeService.selectApplication(rootAid)) {
                System.out.println("Root application selected successfully");
            } else {
                System.out.println("Failed to select root application");
            }
            
        } catch (CardException e) {
            log.error("Card communication error", e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Unexpected error", e);
            e.printStackTrace();
        } finally {
            // Disconnect from card
            if (completeService.isConnected()) {
                completeService.disconnect();
                log.info("Disconnected from card");
            }
        }
    }


}

