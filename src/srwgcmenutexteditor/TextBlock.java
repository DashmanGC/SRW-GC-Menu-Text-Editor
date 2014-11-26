/*
 * Copyright (C) 2014 Dashman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package srwgcmenutexteditor;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonatan
 */
public class TextBlock{
    private int position;       // The position in the offsets array --- DIDN'T USE IT IN THE END!!
    private int table_size;     // Size of the table in bytes
    private String[] entries;   // Number of entries = table_size / 4 (4 bytes per pointer)
                                // Entries don't include the empty bytes (00 00) separating them.

    TextBlock(){
        position = 0;
        table_size = 0;
        entries = null;
    }

    TextBlock(int pos, int size){
        position = pos;
        table_size = size;
        entries = new String[size / 4];
    }

    // Gets and Sets
    public int getPosition(){ return position; }
    public void setPosition(int pos) { position = pos; }

    public int getTableSize() { return table_size; }
    public void setTableSize(int size){
        if (size % 4 != 0){
            System.err.println("Block: " + position + " - Assigned wrong table size.");
            return;
        }
        table_size = size;
        entries = new String[size / 4];
    }

    public String getEntry(int pos){
        if ( pos > (entries.length - 1) ){
            System.err.println("Block: " + position + " - Trying to read outside the array.");
            return null;
        }
        return entries[pos];
    }
    public void setEntry(int pos, String s){
        if ( pos > (entries.length - 1) ){
            System.err.println("Block: " + position + " - Trying to assign a String outside the array.");
            return;
        }
        entries[pos] = s;
    }

    // Use this when reading from add02dat.bin
    // The byte array containing the raw SJIS text will be stored in a String, properly formatted
    public boolean hex2string(int pos, byte[] sjis_text){
        boolean success = false;

        if ( pos > (entries.length - 1) ){
            System.err.println("Block: " + position + " - Trying to assign a String outside the array.");
            return success;
        }

        try {
            entries[pos] = new String(sjis_text, "Shift-JIS");
            success = true;
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Block: " + position + ", Entry: " + pos + " - NOT SJIS!!");
            //Logger.getLogger(TextBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    // Use this when writing the data into a file
    // Formats our stored SJIS string into a byte array
    public byte[] string2hex(int pos){
        byte [] rawHex = new byte[1];   // Returning a 1-byte array will be considered an error

        if ( pos > (entries.length - 1) ){
            System.err.println("Block: " + position + " - Trying to read outside the array.");
            return rawHex;
        }

        try {
            rawHex = entries[pos].getBytes("Shift-JIS");
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Block: " + position + ", Entry: " + pos + " - Couldn't write!!");
            //Logger.getLogger(TextBlock.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rawHex;
    }

    // Use this to calculate the block's size before reinsertion
    // Returns: table_size + the sum of the bytes in each entry + 2 per entry + padding
    public int getBlockSize(){
        int size = 0;

        size += table_size;

        /*
         * IMPORTANT:
         * Apparently each of the bytes of the carriage return (0d 0a)
         * counts as one character!
         * We have to try to split the string so that we don't count
         * each carriage return as 4 bytes instead of 2
         * NOPE! Just get the bytes
         *
         * IMPORTANT 2:
         * In the list of Stage Titles, the last (dummy?) entries end with
         * 3 1-byte characters (" 00", " 01", " 02" or "03")
         * NOPE! Same as above
         */

        for (int i = 0; i < entries.length; i++){
            try {
                /*int entry_size = entries[i].getBytes("Shift-JIS").length;

                if (entry_size % 2 > 0) // Make sure the entry's size is even (can be odd if we're using ASCII)
                    entry_size++;

                size += entry_size;*/

                size += entries[i].getBytes("Shift-JIS").length;

                size += 2; // The empty bytes (00 00) between entries.
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TextBlock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Size has to be a multiple of 32
        int extraBytes = size % 32;

        if (extraBytes > 0) // If we're not 32-byte aligned
            size += 32 - extraBytes;    // Add padding to the size

        return size;
    }

    // Return this block as a byte-array
    public byte[] getBytes(){
        byte[] block = new byte[getBlockSize()];

        // The first pointer in the table is the table size
        byte[] offset = int2bytes(table_size);

        block[0] = offset[0];
        block[1] = offset[1];
        block[2] = offset[2];
        block[3] = offset[3];

        int accumulated = table_size;

        /*
         * IMPORTANT:
         * Apparently each of the bytes of the carriage return (0d 0a)
         * counts as one character!
         * We have to try to split the string so that we don't count
         * each carriage return as 4 bytes instead of 2
         * NOPE! Just get the bytes.
         */

        // Write the rest of the pointer table
        for (int i = 0; i < table_size / 4 - 1; i++){   // Don't read the last entry
            try {
                int entry_size = entries[i].getBytes("Shift-JIS").length;

                // We make this sure when saving the strings
                //if (entry_size % 2 > 0) // Make sure the entry's size is even (can be odd if we're using ASCII)
                //    entry_size++;

                accumulated += entry_size;

                accumulated += 2; // Separator
                
                offset = int2bytes(accumulated);

                block[(i + 1) * 4] = offset[0];
                block[(i + 1) * 4 + 1] = offset[1];
                block[(i + 1) * 4 + 2] = offset[2];
                block[(i + 1) * 4 + 3] = offset[3];
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TextBlock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        accumulated = table_size;
        byte [] data = new byte[1];

        // Write the entries
        for (int i = 0; i < table_size / 4; i++){
            try {
                // We do this when saving the strings
                /*byte[] predata = entries[i].getBytes("Shift-JIS");

                if (predata.length % 2 > 0){    // Make sure we're storing an even number of bytes
                    data = new byte[predata.length + 1];

                    for (int j = 0; j < predata.length; j++)
                        data[j] = predata[j];

                    data[data.length - 1] = 0x20;   // Add an empty space at the end
                }
                else
                    data = predata;*/

                data = entries[i].getBytes("Shift-JIS");

                replaceSpecialChars(data);  // Replace special characters

                for (int j = 0; j < data.length; j++)
                    block[accumulated + j] = data[j];

                accumulated += data.length;
                accumulated += 2;   // Separator
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TextBlock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return block;
    }

    // Receives an int and return its 4-byte value
    private byte[] int2bytes(int value){
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
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
