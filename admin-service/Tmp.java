public class Tmp {
  static class UnauthorizedException extends RuntimeException {}
  void f(Throwable ex){
    if (ex instanceof UnauthorizedException unauthorizedException) {
      throw unauthorizedException;
    }
  }
}
