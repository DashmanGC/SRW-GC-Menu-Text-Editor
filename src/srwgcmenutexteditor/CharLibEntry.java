/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package srwgcmenutexteditor;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonatan
 */
public class CharLibEntry {
    byte[] preamble;    // Always 8 bytes
    String hiragana;    // Character name in hiragana
    String seiyuu;      // Character's Voice Actor
    String description; // Library info for this entry

    /*
     * I'm leaving the variables public and not putting get/set functions
     * There's pretty much nothing to control with these variables when accessing them
     */

    CharLibEntry(){
        preamble = null;
        hiragana = "";
        seiyuu = "";
        description = "";
    }

    /*
     * Data will be set externally during parsing of the file
     */


    // Use this when reinserting data
    // It returns the whole entry as a byte array, separators included
    public byte[] getBytes(){
        byte[] result = null;

        if (preamble == null){
            System.err.println("WARNING: Character Library entry not initialized.");
            return result;
        }

        try {
            // Get the total size of the entry
            int size = 0;

            byte[] hex_hiragana = hiragana.getBytes("Shift-JIS");
            byte[] hex_seiyuu = seiyuu.getBytes("Shift-JIS");
            byte[] hex_description = description.getBytes("Shift-JIS");

            size += 8; // Preamble bytes
            //size += hiragana.length() * 2;  // 2-byte characters
            size += hex_hiragana.length;  // It can contain 1-byte AND 2-byte characters
            size += 2; // Separator (00 00)
            //size += seiyuu.length() * 2;
            
            // Write the offset of the seiyuu inside the preamble (bytes 4 - 5)
            preamble[4] = (byte) ( (size >> 8) & 0xff);
            preamble[5] = (byte) ( size & 0xff);
            
            size += hex_seiyuu.length;
            size += 2;
            
            // Write the offset of the description inside the preamble (bytes 6 - 7)
            preamble[6] = (byte) ( (size >> 8) & 0xff);
            preamble[7] = (byte) ( size & 0xff);
            
            //size += description.length() * 2;
            size += hex_description.length;
            size += 2;

            // Reserve memory for the entry and fill it up
            result = new byte[size];

            for (int i = 0; i < 8; i++)
                result[i] = preamble[i];

            int accumulated = 8;    // accumulated offset

            //byte[] hex_hiragana = hiragana.getBytes("Shift-JIS");

            for (int i = 0; i < hex_hiragana.length; i++)
                result[accumulated + i] = hex_hiragana[i];

            accumulated += hex_hiragana.length;

            // Separator
            accumulated += 2;

            //byte[] hex_seiyuu = seiyuu.getBytes("Shift-JIS");

            for (int i = 0; i < hex_seiyuu.length; i++)
                result[accumulated + i] = hex_seiyuu[i];

            accumulated += hex_seiyuu.length;

            // Separator
            accumulated += 2;

            //byte[] hex_description = description.getBytes("Shift-JIS");

            replaceSpecialChars(hex_description);   // Replace special chars (if they're present)

            for (int i = 0; i < hex_description.length; i++)
                result[accumulated + i] = hex_description[i];

            accumulated += hex_description.length;

            // Done.
            
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Character Library entry contains badly coded SJIS.");
            return null;
            //Logger.getLogger(CharLibEntry.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    // Replaces the conflictive SJIS characters in a block BEFORE REINSERTION
    // When parsing the file, the following characters are transformed:
    // Ⅰ (87 54) -> Ι (83 a7) * this one is used only in the description of the Deathgrome II
    // Ⅱ (87 55) -> П (84 50)
    // Ⅲ (87 56) -> Ш (84 59)
    // "inverted omega" (eb 3f) -> Ю (84 5f)
    // During reinsertion, the values are reversed (this function only deals with reinsertion!)
    // This change is necessary because otherwise, the application doesn't recognize
    // the original characters and the data is destroyed during reinsertion
    // (they're transformed to 1-byte "?" (3f) characters)
    private byte[] replaceSpecialChars(byte[] block){
        byte[] block2 = new byte[2];

        byte[] origI = new byte[]{(byte) 0x87, (byte) 0x54};
        byte[] origII = new byte[]{(byte) 0x87, (byte) 0x55};
        byte[] origIII = new byte[]{(byte) 0x87, (byte) 0x56};
        byte[] origGameo = new byte[]{(byte) 0xeb, (byte) 0x3f};

        byte[] newI = new byte[]{(byte) 0x83, (byte) 0xa7};
        byte[] newII = new byte[]{(byte) 0x84, (byte) 0x50};
        byte[] newIII = new byte[]{(byte) 0x84, (byte) 0x59};
        byte[] newGameo = new byte[]{(byte) 0x84, (byte) 0x5f};

        boolean stop = false;

        for (int i = 0; i < block.length && !stop; i++){
            block2[0] = block[i];
            block2[1] = block[i + 1];

            if (block2[0] == newI[0] && block2[1] == newI[1]){
                block[i] = origI[0];
                block[i + 1] = origI[1];
            }
            else if (block2[0] == newII[0] && block2[1] == newII[1]){
                block[i] = origII[0];
                block[i + 1] = origII[1];
            }
            else if (block2[0] == newIII[0] && block2[1] == newIII[1]){
                block[i] = origIII[0];
                block[i + 1] = origIII[1];
            }
            else if (block2[0] == newGameo[0] && block2[1] == newGameo[1]){
                block[i] = origGameo[0];
                block[i + 1] = origGameo[1];
            }

            // There are some titles that end with 3 1-byte characters, so we have to
            // control we're not getting out of the block
            if (i + 2 > block.length - 1){
                //System.out.println("STOP! Length: " + block.length + " position: " + i);
                stop = true;
            }
        }

        return block;
    }

}
