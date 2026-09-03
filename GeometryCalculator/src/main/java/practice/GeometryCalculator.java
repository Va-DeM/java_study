package practice;

public class GeometryCalculator {

    // если значение radius меньше 0, метод должен вернуть -1
    public static double getCircleSquare(double radius) {
        if (radius < 0)  {
            return -1;
        }
        else {
            return Math.PI * Math.pow(radius, 2);
        }
    }

    // если значение radius меньше 0, метод должен вернуть -1
    public static double getSphereVolume(double radius) {
        if (radius < 0) {
            return -1;
        }
        else {
            return (4 * Math.PI * Math.pow(radius, 3)) / 3;
        }

    }

    public static boolean isTrianglePossible(double a, double b, double c) {
        if (a+b > c && a+c > b && b+c > a) {return true;}
        return false;
    }

    // перед расчетом площади рекомендуется проверить возможен ли такой треугольник
    // методом isTrianglePossible, если невозможен вернуть -1.0
    public static double getTriangleSquare(double a, double b, double c) {
        boolean hasTriangle = isTrianglePossible(a, b, c);
        if (hasTriangle) {
            double halfPerimeter = (a + b + c) / 2;
            return Math.sqrt((halfPerimeter *
                            (halfPerimeter - a) *
                            (halfPerimeter - b) *
                            (halfPerimeter - c)));
        }
        return -1;
    }
}
