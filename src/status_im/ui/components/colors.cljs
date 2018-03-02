(ns status-im.ui.components.colors
  (:require [clojure.string :as string]))

(def white "#ffffff")
(def white-light-transparent "rgba(255, 255, 255, 0.1)") ;; Used as icon background color for a dark foreground
(def white-transparent "rgba(255, 255, 255, 0.2)") ;; Used as icon color on dark background
(def white-lighter-transparent "rgba(255, 255, 255, 0.6)") ;; Used for input placeholder color
(def black "#000000") ;; Used as the default text color
(def black-transparent "#00000020") ;; Used as background color for rounded button on dark background
(def gray "#939ba1") ;; Used as a background for a light foreground and as section header and secondary text color
(def gray-icon "#6e777e") ;; Used for forward icon in accounts
(def gray-light "#e8ebec") ;; Used as divider color
(def gray-lighter "#eef2f5")  ;; Used as a background or shadow
(def gray-dark "#2f3031")  ;; Used as default status bar background color for modal view
(def blue "#4360df") ;; Used as main wallet color, and ios home add button
(def red "#ff2d55") ;; Used to highlight errors or "dangerous" actions
(def red-light "#ffe5ea") ;; error tooltip
(def text-light-gray "#212121") ;; Used for labels (home items)

(defn alpha [hex opacity]
  (let [hex (string/replace hex #"#" "")
        r (js/parseInt (subs hex 0 2) 16)
        g (js/parseInt (subs hex 2 4) 16)
        b (js/parseInt (subs hex 4 6) 16)]
    (str "rgba(" r "," g "," b "," opacity")")))

(def text black)
