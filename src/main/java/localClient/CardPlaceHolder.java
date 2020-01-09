package localClient;

public class CardPlaceHolder implements CardInterface {
    // TODO change this for the real class
    @Override
    public String getCardInfos(String pinCode) {
        return pinCode + ",default";
    }
}
