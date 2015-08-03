package me.fru1t.rsbot.common.food;

public enum AllFood {
	ANCHOVIES(319 , 200, "Anchovies"),
	BREAD(2309 , 200, "Bread"),
	CRAYFISH(13433, 200, "Crayfish"),
	COOKED_CHICKEN(2140 , 200, "Cooked Chicken"),
	COOKED_MEAT(2142 , 200, "Cooked Meat"),
	SHRIMP(315 , 200, "Shrimp"),
	SARDINE(325 , 200, "Sardine"),
	HERRING(347 , 200, "Herring"),
	MACKEREL(355 , 200, "Mackerel"),
	TROUT(333 , 375, "Trout"),
	COD(339 , 450, "Cod"),
	PIKE(351 , 500, "Pike"),
	SALMON(329 , 625, "Salmon"),
	TUNA(361 , 750, "Tuna"),
	BASS(365 , 1300, "Bass"),
	LOBSTER(379 , 1200, "Lobster"),
	SWORDFISH(373 , 1400, "Swordfish"),
	MONKFISH(7947, 1600, "Monkfish"),
	SHARK(385 , 2000, "Shark"),
	SEA_TURTLE(397 , 2000, "Sea Turtle"),
	CAVEFISH(15266, 2200, "Cavefish"),
	MANTA_RAY(391 , 2275, "Manta Ray"),
	ROCKTAIL(15272, 2300, "Rocktail"),
	POTATO_WITH_CHEESE(6705 , 1175, "Potato With Cheese"),
	EGG_POTATO(7056 , 1375, "Egg Potato"),
	TUNA_POTATO(7060 , 2125, "Tuna Potato"),
	TIGER_SHARK(21521, 2375, "Tiger Shark"),
	ROCKTAIL_SOUP(26313, 2500, "Rocktail Soup");
	
	public final int id;
	public final int healAmount;
	public final String displayName;
	private AllFood(int id, int healAmount, String displayName) {
		this.id = id;
		this.healAmount = healAmount;
		this.displayName = displayName;
	}
}
