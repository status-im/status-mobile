(ns status-im.ui.components.colors
  (:require [clojure.string :as string]
            [reagent.core :as reagent]))

(defn alpha [hex opacity]
  (let [hex (string/replace hex #"#" "")
        r   (js/parseInt (subs hex 0 2) 16)
        g   (js/parseInt (subs hex 2 4) 16)
        b   (js/parseInt (subs hex 4 6) 16)]
    (str "rgba(" r "," g "," b "," opacity ")")))

(def dark {:white        "#141414"
           :black        "#ffffff"
           :gray-lighter "#252528"
           :blue         "#6177E5"
           :gray         "#838C91"
           :blue-light   "#23252F"
           :red          "#FC5F5F"})

(def light {:white        "#ffffff"
            :black        "#000000"
            :gray-lighter "#eef2f5"
            :blue         "#4360df"
            :gray         "#939ba1"
            :blue-light   "#ECEFFC"
            :red          "#ff2d55"})

(def themes {:dark dark :light light})

;; WHITE
(def white (:white light))
(def white-persist (:white light))                          ;; this doesn't with theme
(def white-transparent-10 (alpha white 0.1))                ;; Used as icon background color for a dark foreground
(def white-transparent (alpha white 0.4))                   ;; Used as icon color on dark background and input placeholder color
(def white-transparent-persist (alpha white 0.4))
(def white-transparent-70 (alpha white 0.7))
(def white-transparent-70-persist (alpha white 0.7))

(def red-light "#ffe5ea")                                   ;; error tooltip TODO (andrey) should be white, but shadow needed

;; BLACK
(def black (:black light))                                  ;; Used as the default text color
(def black-persist (:white dark))                           ;; this doesn't with theme
(def black-transparent (alpha black 0.1))                   ;; Used as background color for rounded button on dark background and as background color for containers like "Backup recovery phrase"
(def black-transparent-20 (alpha black 0.2))                ; accounts divider
(def black-transparent-40 (alpha black 0.4))
(def black-transparent-40-persist (alpha black 0.4))
(def black-transparent-50 (alpha black 0.5))
(def black-light "#2d2d2d")                                 ;; sign-with-keycard-button
(def black-transparent-86 "rgba(0, 0, 0, 0.86)")

;; DARK GREY
(def gray (:gray light))                                    ;; Dark grey, used as a background for a light foreground and as section header and secondary text color
(def gray-transparent-10 (alpha gray 0.1))
(def gray-transparent-40 (alpha gray 0.4))
;; LIGHT GREY
(def gray-lighter (:gray-lighter light))                    ;; Light Grey, used as a background or shadow

;; ACCENT BLUE
(def blue (:blue light))                                    ;; Accent blue, used as main wallet color, and ios home add button
(def blue-persist (:blue light))
;; LIGHT BLUE
(def blue-light (:blue-light light))                        ;; Light Blue
(def blue-transparent-10 (alpha blue 0.1))                  ;; unknown

;; RED
(def red (:red light))                                      ;; Used to highlight errors or "dangerous" actions
(def red-transparent-10 (alpha red 0.1))                    ;;action-row ;; ttt finish
(def red-audio-recorder "#fa6565")

;; GREEN
(def green "#44d058")                                       ;; icon for successful inboud transaction
(def green-transparent-10 (alpha green 0.1))                ;; icon for successful inboud transaction

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

(def default-chat-color "#a187d5")                          ;; legacy

;; THEME

(def theme (reagent/atom :light))

(defn dark? []
  (= :dark @theme))

(defn set-theme [type]
  (when-not (= type @theme)
    (let [colors (get themes type)]
      (set! white (:white colors))
      (set! black (:black colors))
      (set! gray-lighter (:gray-lighter colors))
      (set! blue (:blue colors))
      (set! gray (:gray colors))
      (set! blue-light (:blue-light colors))
      (set! red (:red colors))
      (set! text black)
      (set! white-transparent-10 (alpha white 0.1))
      (set! white-transparent (alpha white 0.4))
      (set! white-transparent-70 (alpha white 0.7))
      (set! black-transparent (alpha black 0.1))
      (set! black-transparent-20 (alpha black 0.2))
      (set! black-transparent-40 (alpha black 0.4))
      (set! black-transparent-50 (alpha black 0.5))
      (set! gray-transparent-10 (alpha gray 0.1))
      (set! gray-transparent-40 (alpha gray 0.4))
      (set! green-transparent-10 (alpha green 0.1))
      (set! red-transparent-10 (alpha red 0.1))
      (set! blue-transparent-10 (alpha blue 0.1)))
    (reset! theme type)))
