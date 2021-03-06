(ns client.game.paint
  (:require
    [client.game.multiplayer :refer [opponent-scale]]
    [client.game.board :refer [empty-board
                               read-board
                               board-size
                               piece-type-adj]]))

;;------------------------------------------------------------
;; PAINTING (for showing the game on a canvas)
;;------------------------------------------------------------

(def tilemap-tengen (let [img (js/Image.)]
                      (aset img "src" "tilemap-tengen.png")
                      img))

(def tilemap-gameboy (let [img (js/Image.)]
                      (aset img "src" "tilemap-gameboy.png")
                      img))

(def tilemap-gameboy-color (let [img (js/Image.)]
                      (aset img "src" "tilemap-gameboy-color.png")
                      img))

(def tilemap (let [img (js/Image.)]
               (aset img "src" "tilemap.png")
               img))

; The size of a cell in pixels.
(def cell-size 32)

(def value-position
  "An ordering imposed on the possible cell types, used for tilemap position."
  { 0 0
   :I 1
   :L 2
   :J 3
   :S 4
   :Z 5
   :O 6
   :T 7
   :G 8  ; ghost piece
   :H 9  ; highlighted (filled or about to collapse)
   })

(defn get-image-region
  "Get the tilemap and position for the image of the given cell value and theme."
  [theme value]
  (let [wrap-theme (mod theme 10)
        string-value (str value)]
    (cond 
      (= wrap-theme 2)
        (let [[k a] (piece-type-adj value)
              row (value-position k)
              col a]
          [tilemap-tengen row col])
      (and (= wrap-theme 6) (= (subs string-value 0 1) "I"))
        (let [[k a] (piece-type-adj value)
              row (value-position k)
              col a]
          [tilemap-gameboy-color row col])
      (and (= wrap-theme 3) (= (subs string-value 0 1) "I"))
        (let [[k a] (piece-type-adj value)
              row (value-position k)
              col a]
          [tilemap-gameboy row col])
      :else
        (let [[k _] (piece-type-adj value)
              row wrap-theme
              col (value-position k)]
          [tilemap row col]))
    )
  )

(defn size-canvas!
  "Set the size of the canvas."
  ([id board scale] (size-canvas! id board scale 0))
  ([id board scale y-cutoff]
   (let [canvas (.getElementById js/document id)
         [w h] (board-size board)]
     (aset canvas "width" (* scale w))
     (aset canvas "height" (* scale (- h y-cutoff))))))

(defn draw-board!
  "Draw the given board to the canvas."
  ([id board scale theme] (draw-board! id board scale theme 0))
  ([id board scale theme y-cutoff]
    (let [canvas (.getElementById js/document id)
          ctx (.getContext canvas "2d")
          [w h] (board-size board)]
      (doseq [x (range w) y (range h)]
        (let [; tilemap region
              [img row col] (get-image-region theme (read-board x y board))

              ; source coordinates (on (draw-board! "game-canvas" new-board cell-size (:level @state) rows-cutoff))
              sx (* cell-size col) ; Cell-size is based on tilemap, always extract with that size
              sy (* cell-size row)
              sw cell-size
              sh cell-size

              ; destination coordinates (on canvas)
              dx (* scale x)
              dy  (* scale (- y y-cutoff))
              dw scale
              dh scale]

          (.drawImage ctx img sx sy sw sh dx dy dw dh)))
      nil)))

(defn create-opponent-canvas!
  "Draw each opponents board"
  [id]
  (if (nil? (.getElementById js/document id))
    (let [arena (.getElementById js/document "arena")
          canvas (.createElement js/document "canvas")]
      (.appendChild arena canvas)
      (aset canvas "id" id)
      (size-canvas! id empty-board (opponent-scale cell-size))
      )))

(defn delete-opponent-canvas!
  [id]
  (let [arena (.getElementById js/document "arena")
        canvas (.getElementById js/document id)]
    (if-not (nil? canvas)
      (.removeChild arena canvas))))
