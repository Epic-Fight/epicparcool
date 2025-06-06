package com.yesman.epicparcool;

import yesman.epicfight.api.animation.LivingMotion;

public enum ParcoolLivingMotions implements LivingMotion {
	CLING_TO_CLIFF,
	CLING_TO_CLIFF_INNER_CORNER,
	CLING_TO_CLIFF_OUTER_CORNER,
	CLING_TO_CLIFF_LEFT,
	CLING_TO_CLIFF_RIGHT,
	DIVE,
	SKY_DIVE,
	WALL_SLIDING_LEFT,
	WALL_SLIDING_RIGHT,
	WALL_RUNNING_LEFT,
	WALL_RUNNING_RIGHT,
	FAST_RUN,
	HANG_DOWN_ORTHOGONAL,
	HANG_DOWN,
	SLIDING,
	CRAWL,
	CAT_LEAP_PREPARATION,
	HIDE_IN_BLOCK_HORIZONTAL,
	RIDE_ZIPLINE_FORWARD,
	RIDE_ZIPLINE_SIDE;
	
	final int id;
	
	ParcoolLivingMotions() {
		this.id = LivingMotion.ENUM_MANAGER.assign(this);
	}
	
	public int universalOrdinal() {
		return this.id;
	}
}