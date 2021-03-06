import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class WaveDisplayer extends JFrame {
    private FileInputStream fis;
    private BufferedInputStream bis;
    private int[] samples; //data
    private int maxValue; //the maximum value of the samples
    private int sampleCount; //the number of samples

    public int twoBytesToInt(byte[] bytes, int offset) {
        return ((int)(bytes[offset]) << 8) | (bytes[offset - 1] & 0xFF);
    }

    public int fourBytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset - 1] & 0xFF) << 16) |
                ((bytes[offset - 2] & 0xFF) << 8) | (bytes[offset - 3] & 0xFF);
    }

    public WaveDisplayer(String fileName) throws IOException {
        fis = new FileInputStream(fileName);
        bis = new BufferedInputStream(fis);

        byte[] header = new byte[44]; //the first 44 bytes are the header
        bis.read(header, 0, 44);

        int sampleSize = twoBytesToInt(header, 35); //the number of bits per sample
        int dataSize = fourBytesToInt(header, 43); //the number of bytes of the data area
        sampleCount = dataSize / (sampleSize / 8);

        samples = new int[sampleCount];

        if(sampleSize == 8) { //1 byte per sample
            for(int i = 0; i < sampleCount; i++) {
                samples[i] = bis.read();
            }
        } else { //2 bytes per sample
            byte[] temp = new byte[2];
            for(int i = 0; i < sampleCount; i++) {
                bis.read(temp);
                samples[i] = twoBytesToInt(temp, 1);
            }
        }

        findMaxValue(); //find the maximum value of the samples

        fis.close();
        bis.close();
    }

    public void findMaxValue() { //find the maximum value of the samples
        maxValue = Integer.MIN_VALUE;

        for(int elem : samples) {
            maxValue = Integer.max(maxValue, elem);
        }
    }

    public void showWave(int width, int height) {
        this.setTitle("Wave Displayer");
        this.setSize(width, height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        Waveform waveform = new Waveform();
        Dimension dimension = new Dimension(width, height);
        waveform.setPreferredSize(dimension);
        this.add(waveform);
        this.setVisible(true);
    }

    public class Waveform extends JPanel {
        public void paint(Graphics g) {
            super.paint(g);
            int width = getWidth();
            int height = getHeight();
            int step = sampleCount / width; //to display the whole waveform, need to get evenly spaced samples
            double intervalY = height / 2.0 / maxValue / 1.5;
            int preX, preY, curX, curY;

            preX = preY = 0;
            g.setColor(new Color(30, 100, 50));

            for(int i = 0; i < width; i++) {
                curX = i; //x coordinate of the current sample
                curY = height - (int)(samples[i * step] * intervalY + height / 2); //y coordinate of the current sample

                g.drawLine(preX, preY, curX, curY); //link the previous sample and the current sample

                preX = curX; //x coordinate of the previous sample
                preY = curY; //y coordinate of the previous sample
            }

            g.drawString("Maximum value: " + maxValue, 10, 15);
            g.drawString("The number of samples: " + sampleCount, 10, 35);
        }
    }

    public static void main(String[] args) {
        try {
            FileSelector fs = new FileSelector();
            if(fs.getPath() != null && fs.getPath().endsWith(".wav")) {
                WaveDisplayer wd = new WaveDisplayer(fs.getPath());
                wd.showWave(1900, 400);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
