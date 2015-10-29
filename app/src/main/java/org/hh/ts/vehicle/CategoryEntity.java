package org.hh.ts.vehicle;

public class CategoryEntity {
	private String categoryName;
	private int resXmlId;
	private int resPicId;
	public CategoryEntity(String categoryName, int resXmlId, int resPicId) {
		this.categoryName = categoryName;
		this.resXmlId = resXmlId;
		this.resPicId = resPicId;
	}
	@Override
	public boolean equals(Object obj) {
		 if (this==obj) return true;
		 if (!(obj instanceof CategoryEntity)) return false;
		 final CategoryEntity other=(CategoryEntity)obj;
		 if(other.getName().equals(this.getName())) {
			 return true;
		 } else return false;
	}
	public String getName() {
		return categoryName;
	}
	public int getResXmlId() {
		return resXmlId;
	}
	public int getResPicId() {
		return resPicId;
	}
}
