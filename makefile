all: comp
	java -cp bin main/App
comp:
	javac -d bin -cp bin -sourcepath src/main src/main/*.java src/main/board/*.java src/main/board/characters/*.java src/main/board/items/*.java src/main/board/scenery/*.java src/main/board/baseclasses/*.java src/main/board/combat/*.java src/main/frontend/*.java src/main/frontend/terminal/*.java