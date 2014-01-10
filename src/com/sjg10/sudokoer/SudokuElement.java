/**
 *  Sudokoer
 *  Component SudokuGrid
 *  (C) 2014 by Samuel Gonshaw (sjg10@imperial.ac.uk) and Yi Zhang (yi.zhang7210@gmail.com)
 *  
 *  Sudokoer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Sudokoer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Sudokoer.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sjg10.sudokoer;

public class SudokuElement {
	public int location[];
	public int content;
	public boolean isDefinite;
	
	/* Constructor */
	public SudokuElement(int[] location, int content, boolean isDefinite){
		this.location = location.clone();
		this.content = content;
		this.isDefinite = isDefinite;
	}
	
	@Override
	public String toString(){
		return "pos=("+Integer.toString(location[0])+","+Integer.toString(location[1])+
				") content="+Integer.toString(content)+" isDefinite="+Boolean.toString(isDefinite);
	}
	

}
