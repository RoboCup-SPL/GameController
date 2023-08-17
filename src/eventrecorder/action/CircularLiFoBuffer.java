package eventrecorder.action;

/**
 * Circular-Last-In-First-Out-Buffer specially for redo/undo-functionality.
 *
 * @author Andre Muehlenbrock
 */

public class CircularLiFoBuffer<T> {
    final T[] array;
    int pos = -1;
    int elements = 0;
    int pops = 0;

    @SuppressWarnings("unchecked")
    public CircularLiFoBuffer(int size){
        array = (T[]) new Object[size];
    }

    public T get(int p){
        return array[(array.length+pos-p)%array.length];
    }

    public void push(T t){
        pops = 0;
        pos++;

        if(pos >= array.length){
            pos = 0;
        }

        array[pos] = t;


        if(elements < array.length){
            elements++;
        }
    }

    public T peek(){
        if(elements > 0){
            return array[pos];
        }

        return null;
    }

    public T pop(){
        T t = peek();
        if(t != null){
            pops++;
            elements--;
            pos--;
            if(pos < 0)
                pos=array.length-1;
            return t;
        }
        return null;
    }

    public T popForward(){
        if(pops > 0){
            pops--;
            elements++;
            pos++;
            if(pos >= array.length){
                pos = 0;
            }
            return array[pos];
        }

        return null;
    }

    public boolean isEmpty(){
        return elements <= 0;
    }

    public boolean hasNext(){
        return pops > 0;
    }
}
