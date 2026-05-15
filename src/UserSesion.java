public class UserSesion {

	private static UserSesion instance;
	private int id;

	private UserSesion(int id) {
		this.id = id;
	}

	public static void startSesion(int id) {
		instance = new UserSesion(id);
	}

	public int getId() {
		return this.id;
	}

	public static UserSesion getInstance() {
		return instance;
	}

	public static void clearInstance() {
		instance = null;
	}

}
