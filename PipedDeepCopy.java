import java.io.*;

class PipedDeepCopy {
    private static final Object ERROR = new Object();

    public static Object copy(Object LostCitiesRivals2PGameState) throws IOException {
        Object obj = null;
        try {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(in);

            Deserializer des = new Deserializer(in);
            ObjectOutputStream out = new ObjectOutputStream(pos);
            out.writeObject(LostCitiesRivals2PGameState);

            obj = des.getDeserializedObject();

            if (obj == ERROR )
                obj = null;


        }
        catch (IOException ioe ) {
            ioe.printStackTrace();
        }
        return obj;

    }

    private static class Deserializer extends Thread {
        /**
         * Object that we are deserializing
         */
        private Object obj = null;

        /**
         * Lock that we block on while deserialization is happening
         */
        private Object lock = null;

        /**
         * InputStream that the object is deserialized from.
         */
        private PipedInputStream in = null;

        public Deserializer(PipedInputStream pin) throws IOException {
            lock = new Object();
            this.in = pin;
            start();
        }

        public void run() {
            Object o = null;
            try {
                ObjectInputStream oin = new ObjectInputStream(in);
                o = oin.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // This should never happen. If it does we make sure
                // that a the object is set to a flag that indicates
                // deserialization was not possible.
                e.printStackTrace();
            } // Same here...


            synchronized (lock) {
                if (o == null)
                    obj = ERROR;
                else
                    obj = o;
                lock.notifyAll();
            }
        }

        /**
         * Returns the deserialized object. This method will block until
         * the object is actually available.
         */
        public Object getDeserializedObject() {
            // Wait for the object to show up
            try {
                synchronized (lock) {
                    while (obj == null) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException ie) {
                // If we are interrupted we just return null
            }
            return obj;
        }
    }
}
