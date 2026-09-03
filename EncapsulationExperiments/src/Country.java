public class Country {
    private String countryName;
    private int population;
    private Double countrySquare;
    private String capital;
    private boolean isAccessToSea;

    public Country(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Double getCountrySquare() {
        return countrySquare;
    }

    public void setCountrySquare(Double countrySquare) {
        this.countrySquare = countrySquare;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public boolean isAccessToSea() {
        return isAccessToSea;
    }

    public void setAccessToSea(boolean accessToSea) {
        isAccessToSea = accessToSea;
    }
}
