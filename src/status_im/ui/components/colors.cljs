(ns status-im.ui.components.colors
  (:require [clojure.string :as string]))

(defn alpha [hex opacity]
  (let [hex (string/replace hex #"#" "")
        r (js/parseInt (subs hex 0 2) 16)
        g (js/parseInt (subs hex 2 4) 16)
        b (js/parseInt (subs hex 4 6) 16)]
    (str "rgba(" r "," g "," b "," opacity ")")))

;; TRANSPARENT
(def transparent "transparent")

;; WHITE
(def white "#ffffff")
(def white-transparent-10 (alpha white 0.1)) ;; Used as icon background color for a dark foreground
(def white-transparent (alpha white 0.4)) ;; Used as icon color on dark background and input placeholder color
(def white-transparent-70 (alpha white 0.7))

(def red-light "#ffe5ea") ;; error tooltip TODO (andrey) should be white, but shadow needed
(def tooltip-green "#e9f6e6") ;; fading tooltip background color TODO (andrey) should be white, but shadow needed

;; BLACK
(def black "#000000") ;; Used as the default text color
(def black-transparent (alpha black 0.1)) ;; Used as background color for rounded button on dark background and as background color for containers like "Backup recovery phrase"
(def black-transparent-20 (alpha black 0.2)) ; accounts divider
(def black-transparent-40 (alpha black 0.4))
(def black-transparent-50 (alpha black 0.5))
(def black-light "#2d2d2d") ;; sign-with-keycard-button

;; DARK GREY
(def gray "#939ba1") ;; Dark grey, used as a background for a light foreground and as section header and secondary text color
(def gray-transparent-10 (alpha gray 0.1))
(def gray-transparent-40 (alpha gray 0.4))
;; LIGHT GREY
(def gray-lighter "#eef2f5")  ;; Light Grey, used as a background or shadow

;; ACCENT BLUE
(def blue "#4360df") ;; Accent blue, used as main wallet color, and ios home add button
;; LIGHT BLUE
(def blue-light "#ECEFFC") ;; Light Blue
(def blue-transparent-10 (alpha blue 0.1)) ;; unknown

;; RED
(def red "#ff2d55") ;; Used to highlight errors or "dangerous" actions
(def red-transparent-10 (alpha red 0.1)) ;;action-row ;; ttt finish

;; GREEN
(def green "#44d058") ;; icon for successful inboud transaction
(def green-transparent-10 (alpha green 0.1)) ;; icon for successful inboud transaction

(def purple "#887af9")
(def orange "#FE8F59")

(def chat-colors ["#fa6565"
                  "#7cda00"
                  purple
                  "#51d0f0"
                  orange
                  "#d37ef4"])

(def account-colors ["#9B832F"
                     "#D37EF4"
                     "#1D806F"
                     "#FA6565"
                     "#7CDA00"
                     purple
                     "#8B3131"])

(def mention-incoming "#0DA4C9")
(def mention-outgoing "#9EE8FA")
(def text black)
(def text-gray gray)

(def default-chat-color "#a187d5") ;; legacy
