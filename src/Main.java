import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

class node{
    public ArrayList<img> subset ;
    public double[][] core ;
    public String code ;
    public node left;
    public node right;
    public node(double[][] arr){
        core = arr;
        subset = new ArrayList<>();
    }
    public void add(img i){
        subset.add(i);
    }
}
class img  {
    node node ;
    BufferedImage block ;
    public img(){
        node = null;
        block = null;
    }

}
public class Main {
    final static int BLOCK_SIZE = 2 ; // BS
    static int nBlockRowCol ;
    static int nBlockImage ;
    static int codeBookSize = 128 ; // CB
    // S
    public static img[] loadImage(BufferedImage imagePath) {
        BufferedImage image = imagePath ;
        // Get image dimensions
        // suppose they both equal and even


        int width = image.getWidth();
        int height = image.getHeight();
        // 16 32 48 64 128
        nBlockRowCol = width / BLOCK_SIZE ;
        //int nBlock = height / BLOCK_SIZE ;
        nBlockImage = nBlockRowCol * nBlockRowCol ;

        img[] block = new img[nBlockRowCol * nBlockRowCol] ;
        int k = 0 ,  m =  0 ;
        for (int i = 0; i < nBlockRowCol * nBlockRowCol; i++) {
            block[i] = new img();
            block[i].block =  image.getSubimage(m,k,BLOCK_SIZE,BLOCK_SIZE);
            m += BLOCK_SIZE;
            if(m == width){
                m = 0;
                k += BLOCK_SIZE;
            }
        }
        return block;
        // Now you have the pixel data stored in the 'pixels' array
        // You can perform further processing or analysis as needed
    }
    public static ArrayList<node> LBG(img[] BLOCKS){
        // calculate Avg of all BLOCKS
        node root = new node(null);
        double[][] Avg ;
        root.subset = new ArrayList<>(Arrays.asList(BLOCKS));

        int level = 0;
        Queue<node> queue = new LinkedList<>();
        queue.add(root);
        ArrayList<node> levelNodes = new ArrayList<>();
        while(!queue.isEmpty()){
            int level_size = queue.size();
            levelNodes.clear();
            int reomve = 0 ;
            while (level_size-- != 0) {
                // loop for all level

                // if reach k leaves ... Push me again
                node temp = queue.poll();
                if(Math.pow(2,level) >= codeBookSize){
                    queue.add(temp);
                }


                // calc avg of temp blocks
                img[] arr = new img[temp.subset.size()];
                temp.subset.toArray(arr);
                Avg = calcAvg(arr);


                // split into two children
                // if reach k leaves ... put AVG on my node not splitted
                // First Check if my core == my new core "AVG" to check end of looping
                if(Math.pow(2,level) < codeBookSize){
                    double[][] leftAVG = new double[BLOCK_SIZE][BLOCK_SIZE];
                    double[][] rightAVG = new double[BLOCK_SIZE][BLOCK_SIZE];
                    for (int row = 0; row < BLOCK_SIZE; row++) {
                        for (int col = 0; col < BLOCK_SIZE; col++) {
                            if(Avg[row][col] % 1 == 0){
                                // int value
                                leftAVG [row][col] = Avg[row][col]-1;
                                rightAVG[row][col] = Avg[row][col]+1;
                            }
                            else{
                                // float value
                                leftAVG [row][col] = Math.floor(Avg[row][col]);
                                rightAVG[row][col] = Math.ceil(Avg[row][col]);
                            }
                        }
                    }
                    temp.left  = new node(leftAVG);
                    temp.right = new node(rightAVG);

                    queue.add(temp.left);
                    queue.add(temp.right);

                    levelNodes.add(temp.left);
                    levelNodes.add(temp.right);
                }
                else{
                    if(Arrays.deepEquals(Avg, temp.core)){
                        reomve++;
                        System.out.println(reomve);
                    }
                    temp.core = Avg ;
                    levelNodes.add(temp);
                    temp.subset.clear();
                }


            }

            // compare each level with each block
            if(reomve >= codeBookSize){
                break;
            }
            if(Math.pow(2,level) >= codeBookSize){
                double min;
                double myNodeCost = Double.MAX_VALUE ;
                double rightNodeCost = Double.MAX_VALUE ;
                double leftNodeCost = Double.MAX_VALUE ;
                node minPTR ;
                for (int i = 0; i < BLOCKS.length; i++) {
                    // for each block in all BLOCKS
                    min = Double.MAX_VALUE ;
                    minPTR = null ;
                    int indexOfMyNode = levelNodes.indexOf(BLOCKS[i].node);
                    myNodeCost = cost(BLOCKS[i].block,levelNodes.get(indexOfMyNode));
                    min = myNodeCost;
                    minPTR = levelNodes.get(indexOfMyNode);
                    if(indexOfMyNode+1 < levelNodes.size()) {
                        rightNodeCost = cost(BLOCKS[i].block, levelNodes.get(indexOfMyNode + 1));
                        if(rightNodeCost < min){
                            min = rightNodeCost;
                            minPTR = levelNodes.get(indexOfMyNode+1);
                        }
                    }
                    if(indexOfMyNode-1 >= 0) {
                        leftNodeCost = cost(BLOCKS[i].block, levelNodes.get(indexOfMyNode - 1));
                        if(leftNodeCost < min){
                            min = leftNodeCost;
                            minPTR = levelNodes.get(indexOfMyNode-1);
                        }
                    }
                    minPTR.add(BLOCKS[i]);
                    BLOCKS[i].node = minPTR;
                }
            }
            else
            {
                double min  ;
                node minPTR ;
                for (int i = 0; i < BLOCKS.length; i++) {
                    // for each block in all BLOCKS
                    min = Double.MAX_VALUE ;
                    minPTR = null ;
                    for (int leaf = 0; leaf < levelNodes.size(); leaf++) {
                        double x = cost(BLOCKS[i].block,levelNodes.get(leaf));
                        if( x < min){
                            minPTR = levelNodes.get(leaf);
                            min = x ;
                        }
                    }
                    minPTR.add(BLOCKS[i]);
                    BLOCKS[i].node = minPTR;
                }
            }

            level++;
        }

        return levelNodes;
        // levelNodes is code Book

    }
    public static double cost(BufferedImage Block ,node leaf){
        double acc = 0 ;
        double R = 0 ;
        double G = 0 ;
        double B = 0 ;

        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE ; j++) {
//                acc += Math.abs( (Block.getRGB(j,i) & 0xFF) - leaf.core[i][j]);
//                acc += Math.abs( (Block.getRGB(j,i)) - leaf.core[i][j]);
                R = Math.abs( ( ( Block.getRGB(j,i) >> 16) &0x0000FF ) - ( ( (int) leaf.core[i][j] >> 16) & 0x0000FF ) );
                G = Math.abs( ( ( Block.getRGB(j,i) >> 8) &0x0000FF ) - ( ( (int) leaf.core[i][j] >> 8) & 0x0000FF ) );
                B = Math.abs( ( ( Block.getRGB(j,i) ) &0x0000FF ) - ( ( (int) leaf.core[i][j] ) & 0x0000FF ) );
                acc += R + G + B ;
            }
        }
        return acc ;
    }
    public static String intToBinary(int number, int desiredLength) {
        String binaryString = Integer.toBinaryString(number);

        // Check if the binary string needs padding
        if (binaryString.length() < desiredLength) {
            // Calculate the number of zeros to pad
            int zerosToPad = desiredLength - binaryString.length();

            // Pad the binary string with zeros on the left
            binaryString = "0".repeat(zerosToPad) + binaryString;
        }

        return binaryString;
    }
    private static byte[] stringToBytes(String s){
        // Ensure that the binary string length is a multiple of 8

        if (s.length() % 8 != 0) {
            int paddingLength = 8 - s.length()%8;
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < paddingLength; i++) {
                padding.append('0');
            }
            s = s + padding.toString();
        }

        // Create an array to store the bytes
        byte[] byteArray = new byte[s.length() / 8];

        // Convert each group of 8 binary digits to a byte
        for (int i = 0; i < s.length(); i += 8) {
            String binaryByte = s.substring(i, i + 8);
            byte decimalValue = (byte) Integer.parseInt(binaryByte, 2);
            byteArray[i / 8] = decimalValue;
        }

        return byteArray;
    }
    public static double[][] calcAvg(img[] arr){
        double[][] avg = new double[BLOCK_SIZE][BLOCK_SIZE];
        double R  = 0 ;
        double G  = 0 ;
        double B  = 0 ;
        for (int row = 0; row < BLOCK_SIZE; row++) {
            for (int col = 0; col < BLOCK_SIZE; col++) {
                 R  = 0 ;
                 G  = 0 ;
                 B  = 0 ;
                // for loop for each color
                for (int i = 0; i < arr.length; i++) {
//                  avg[row][col] += arr[i].block.getRGB(col,row) & 0xFF;
//                  avg[row][col] += arr[i].block.getRGB(col,row);
                    R += arr[i].block.getRGB(col,row) >> 16  & 0x0000FF;
                    G += arr[i].block.getRGB(col,row) >> 8  & 0x0000FF;
                    B += arr[i].block.getRGB(col,row)   & 0x0000FF;
                }
                int  Ri  = (int)  (R / arr.length );
                int  Gi  = (int) ( G / arr.length  ) ;
                int  Bi  = (int)  (B / arr.length );
                avg[row][col] += Ri<<16 | Gi << 8 | Bi;
//                avg[row][col] = (double) avg[row][col] / arr.length ;
            }
        }
        return avg;
    }
    public static void comp(BufferedImage imagePath) throws IOException {
        img[] BLOCKS = loadImage(imagePath);
        System.out.println(BLOCKS.length);
        ArrayList<node> codeBook  = LBG(BLOCKS);

        int nBit  = (int) Math.ceil(  Math.log(codeBook.size()) / Math.log(2)   );
        System.out.println("nbit =>" + nBit);
        System.out.println("codebook array size =>" + codeBook.size());
        System.out.println("codebook  size =>" + codeBookSize);

        for (int i = 0; i < codeBook.size(); i++) {
            codeBook.get(i).code = intToBinary(i,nBit);
        }
        System.out.println("after assigning code to every codebook");


        // OVERHEAD
        HashMap<String,double[][]> MAP = new HashMap<>();
        for (int i = 0; i < codeBook.size(); i++) {
            MAP.put(codeBook.get(i).code,codeBook.get(i).core);
        }
        System.out.println("creat map");



        // DATA
        String result = "";
        for (int i = 0; i < BLOCKS.length; i++) {
            result += BLOCKS[i].node.code ;
        }
        System.out.println("res len=> " + result.length());

        // writing to FILE
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("photo.VQ"))) {
            oos.writeObject(MAP);

            // write two int OverHead
            oos.writeInt(BLOCK_SIZE);
            oos.writeInt(nBlockRowCol);


            byte[] binaryData = stringToBytes(result);
            System.out.println("final bytes len => " + binaryData.length);
            oos.write(binaryData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void setPixelsFromArray(BufferedImage image, double[][] pixelArray, int startX, int startY) {
        int width = pixelArray.length;
        int height = pixelArray[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgbColor = (int) pixelArray[x][y];
                //int f = (rgbColor << 16) | (rgbColor << 8) | rgbColor ;
//                image.setRGB(startX + x, startY + y, (rgbColor << 16) | (rgbColor << 8) | rgbColor);
                image.setRGB(startX + x, startY + y, rgbColor);
            }
        }
    }
    public static void decomp(String file) throws IOException {
        HashMap<String, double[][]> MAP = new HashMap<>();
        byte[] binaryData ;
        int dimension = 0 ;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Deserialize the object
            Object obj = ois.readObject();

            if (obj instanceof HashMap) {
                MAP = (HashMap<String, double[][]>) obj ;
                System.out.println("map in decomp size => " + MAP.size());
                System.out.println("code book is => " + codeBookSize);
            } else {
                System.out.println("Error happened");
                return;
            }

            // Read the delimiter (0xFF)
            int  DBlockSize = ois.readInt();
            int  DNBlockRowCol = ois.readInt();
            dimension = DNBlockRowCol * DBlockSize ;


            // Read regular binary data
//            binaryData = new byte[ois.available()];
//            ois.read(binaryData);
            // Read regular binary data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[1024]; // or any other suitable buffer size

            while ((bytesRead = ois.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            binaryData = baos.toByteArray();
            System.out.println("final bytes len => " + binaryData.length);

        }
        catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder binaryStringBuilder = new StringBuilder() ;
        for (byte x : binaryData){
            binaryStringBuilder.append(String.format("%8s", Integer.toBinaryString(x & 0xFF)).replace(' ', '0'));

        }
        String data = binaryStringBuilder.toString();
        System.out.println("final data len => "  + data.length());
        BufferedImage image = new BufferedImage(dimension,dimension,BufferedImage.TYPE_INT_RGB);
        String x = "";
        int i = 0;
        int j = i + 1;
        int col = 0 ;
        int row = 0 ;
        while (i < data.length() && j <= data.length() && row < image.getHeight()) {
            x = data.substring(i, j);
            if (MAP.containsKey(x)) {
                // code found
                setPixelsFromArray(image, MAP.get(x),col,row);
                i = j;
                col+=BLOCK_SIZE;
                if(col >= image.getWidth()){
                    col = 0 ;
                    row += BLOCK_SIZE;
                }
            }
            j++;
        }
        System.out.println(i);
        System.out.println(j);

        System.out.println(data.length());

        System.out.println(row);
        System.out.println(col);

        int index  = (int) (Math.random()*1000);
        System.out.println("index is => " + index);
        String path = "photos\\output" + index + ".png" ;
        System.out.println("path is => "  + path);
        ImageIO.write(image, "png", new File(path));

    }

    public static void main(String[] args) throws IOException {
        createAndShowGUI();
    }
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Vector Quantization Compression App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 500));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel row1 = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row1.add(textArea, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse");
        row1.add(browseButton, BorderLayout.EAST);

        JPanel row2 = new JPanel();
        row2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");

        row2.add(compressButton);
        row2.add(decompressButton);

        mainPanel.add(row1);
        mainPanel.add(row2);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("M:\\java library\\VectorQuantization");
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textArea.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement compression logic here
                File file = new File(textArea.getText());
                BufferedImage i;
                try {
                    i = ImageIO.read(file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    comp(i);
                    JOptionPane.showMessageDialog(frame, "Operation was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Operation failed.", "Failure", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement decompression logic here
                try {
//                    file = new FileInputStream(textArea.getText());
                    decomp(textArea.getText());
                    JOptionPane.showMessageDialog(frame, "Operation was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Operation failed.", "Failure", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);

                }
            }
        });

    }
}
