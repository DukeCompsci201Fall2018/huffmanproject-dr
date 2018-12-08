import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;

	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;

	public HuffProcessor() {
		this(0);
	}

	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){
		
		int[] counts = readForCounts(in);
		HuffNode root = makeTreeFromCounts(counts);
		String [] codings = makeCodingsFromTree(root);
		
		out.writeBits(BITS_PER_INT, HUFF_TREE);
		writeReader(root, out);
		
		in.reset();
		writeCompressedBits(codings, in, out);
		out.close();
		
//		while (true){
//			int val = in.readBits(BITS_PER_WORD);
//			if (val == -1) break;
//			out.writeBits(BITS_PER_WORD, val);
//		}
//		out.close();
	}
	private void writeCompressedBits(String[] codings, BitInputStream in, BitOutputStream out) {
		// TODO Auto-generated method stub
		
	}

	private void writeReader(HuffNode root, BitOutputStream out) {
		// TODO Auto-generated method stub
		
	}

	private String[] makeCodingsFromTree(HuffNode root) {
		String[] encodings = new String[ALPH_SIZE + 1];
	    return codingHelper(root,"",encodings);

		
	}

	private String[] codingHelper(HuffNode root, String string, String[] encodings) {
		if (root.myLeft == null && root.myRight == null) {
	        encodings[root.myValue] = string;
	        
	   }
		return encodings;
		
	}

	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
		int [] freqs = new int [ALPH_SIZE + 1];
		freqs[PSEUDO_EOF] = 1;

		for(int i = 0; i < freqs.length; i ++) {
			if (freqs[i] > 0) {
			    pq.add(new HuffNode(i,freqs[i],null,null));

			}
		}
		while (pq.size() > 1) {
		    HuffNode left = pq.remove();
		    HuffNode right = pq.remove();
		    HuffNode t = new HuffNode(0, right.myWeight + left.myWeight, left, right);
		    pq.add(t);
		}
		HuffNode root = pq.remove();
		return root;
	}

	private int[] readForCounts(BitInputStream in) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);
		if (bits !=HUFF_TREE) {
			throw new HuffException("Illegal header starts with" + bits);
		}
		HuffNode root = readTreeReader(in);
		readCompressedBits(root, in, out);
		out.close();
		
		
		//		while (true){
		//			int val = in.readBits(BITS_PER_WORD);
		//			if (val == -1) break;
		//			out.writeBits(BITS_PER_WORD, val);
		//		}
		//		out.close();
	}

	private HuffNode readTreeReader(BitInputStream in) {
		int bit = in.readBits(BITS_PER_WORD+1);
		if (bit == -1) {
			throw new HuffException("Illegal header starts with" + bit);
		}
		if (bit == 0) {
			HuffNode left = readTreeReader(in);
			HuffNode right = readTreeReader(in);
			return new HuffNode(0,0,left,right);
		}
		else {
			int value = in.readBits(BITS_PER_WORD+1);
			return new HuffNode(value,0,null,null);
		}
	}

	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		HuffNode current = root; 
		   while (true) {
		       int bits = in.readBits(1);
		       if (bits == -1) {
		           throw new HuffException("bad input, no PSEUDO_EOF");
		       }
		       else { 
		           if (bits == 0) current = current.myLeft;
		      else current = current.myRight;

		           if (current.myLeft == null && current.myRight == null) {
		               if (current.myValue == PSEUDO_EOF) 
		                   break;   // out of loop
		               else {
		                   out.writeBits(8, current.myValue);
		                   current = root; // start back after leaf
		               }
		           }
		       }
		   }


	}
}