package org.esupportail.desfire;

import org.esupportail.desfire.service.DESFireApduService;
import org.esupportail.desfire.service.PcscUsbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardException;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private PcscUsbService pcscUsbService = new PcscUsbService();

    public static void main(String[] args) {
        System.out.println("aaa");
        new App().run();
    }

    public void run(){
        log.info("app started");
        DESFireApduService service = new DESFireApduService();

        // Get card version
        String versionCommand = service.getVersion();
        System.out.println("Version APDU: " + versionCommand);

        try {
            String cardTerminalName = pcscUsbService.connection();
            log.debug("cardTerminal : " + cardTerminalName);
        } catch (CardException e) {
            log.error("pcsc connection error", e);
            e.printStackTrace();
        }

//        pcscUsbService.connection();

        byte[] rootAid = {0x00, 0x00, 0x00};
        String selectCommand = service.selectApplication(rootAid);
        System.out.println("Select APDU: " + selectCommand);
    }


}

