package pojos.yahoo.financials;

import java.lang.reflect.Field;
import java.util.List;

public class Timeseries{
    public List<Result> result;
    public Object error;

    /*
     * By default, the api provides financial information split into hundreds of different pojos.yahoo.financials.Result objects
     * This method moves all results into one object for more efficient access
     */
    public void preprocessResults() {
        Result targetInstance = this.result.getFirst();
        this.result.forEach(r-> {
            for(Field f : Result.class.getDeclaredFields()){
                try
                {
                    f.setAccessible(true);
                    Object value = f.get(r);
                    if (value != null)
                        f.set(targetInstance, value);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        this.result = List.of(targetInstance);
    }
}
