(ns utils.responsiveness)

(def ^:const IPHONE_11_PRO_VIEWPORT_WIDTH 375)

(defn iphone-11-Pro-20-pixel-from-width
  [window-width]
  ;; Divide iPhone 11 Pro VW by the desired value
  (let [calculate-ratio (/ IPHONE_11_PRO_VIEWPORT_WIDTH 20)]
    ;; Divide window width by the ratio to get a dynamic value. Based on the VW width
    (int (/ window-width calculate-ratio))))
