(ns quo2.foundations.colors
  (:require [clojure.string :as string]
            [quo2.theme :as theme]))

(defn alpha
  [value opacity]
  (when value
    (if (string/starts-with? value "#")
      (let [hex (string/replace value #"#" "")
            r   (js/parseInt (subs hex 0 2) 16)
            g   (js/parseInt (subs hex 2 4) 16)
            b   (js/parseInt (subs hex 4 6) 16)]
        (str "rgba(" r "," g "," b "," opacity ")"))
      (let [rgb (string/split value #",")]
        (str (string/join "," (butlast rgb)) "," opacity ")")))))

(defn- alpha-opaque
  [value opacity]
  (when value
    (if (string/starts-with? value "#")
      (let [hex (string/replace value #"#" "")
            r   (- 255 (* opacity (- 255 (js/parseInt (subs hex 0 2) 16))))
            g   (- 255 (* opacity (- 255 (js/parseInt (subs hex 2 4) 16))))
            b   (- 255 (* opacity (- 255 (js/parseInt (subs hex 4 6) 16))))]
        (str "rgb(" r "," g "," b ")"))
      (let [rgb (string/split value #",")
            r   (- 255 (* opacity (- 255 (get rgb 0))))
            g   (- 255 (* opacity (- 255 (get rgb 1))))
            b   (- 255 (* opacity (- 255 (get rgb 2))))]
        (str "rgb(" r "," g "," b ")")))))

(def theme-alpha
  (memoize
   (fn
     ([color light-opacity dark-opacity]
      (theme-alpha color light-opacity color dark-opacity))
     ([light-color light-opacity dark-color dark-opacity]
      (if (theme/dark?)
        (alpha light-color light-opacity)
        (alpha dark-color dark-opacity))))))


;;;;Neutral

;;Solid


(def neutral-5 "#F5F6F8")
(def neutral-10 "#F0F2F5")
(def neutral-20 "#E7EAEE")
(def neutral-30 "#DCE0E5")
(def neutral-40 "#A1ABBD")
(def neutral-50 "#647084")
(def neutral-60 "#303D55")
(def neutral-70 "#202C42")
(def neutral-80 "#1B273D")
(def neutral-90 "#131D2F")
(def neutral-95 "#0D1625")
(def neutral-100 "#09101C")

;;Blur
(def neutral-5-opa-70 (alpha neutral-5 0.7))
(def neutral-90-opa-70 (alpha neutral-90 0.7))

;;80 with transparency
(def neutral-80-opa-5 (alpha neutral-80 0.05))
(def neutral-80-opa-10 (alpha neutral-80 0.1))
(def neutral-80-opa-15 (alpha neutral-80 0.15))
(def neutral-80-opa-20 (alpha neutral-80 0.2))
(def neutral-80-opa-30 (alpha neutral-80 0.3))
(def neutral-80-opa-40 (alpha neutral-80 0.4))
(def neutral-80-opa-50 (alpha neutral-80 0.4))
(def neutral-80-opa-60 (alpha neutral-80 0.6))
(def neutral-80-opa-70 (alpha neutral-80 0.7))
(def neutral-80-opa-80 (alpha neutral-80 0.8))
(def neutral-80-opa-90 (alpha neutral-80 0.9))
(def neutral-80-opa-95 (alpha neutral-80 0.95))

;;95 with transparency
(def neutral-95-opa-60 (alpha neutral-95 0.6))
(def neutral-95-opa-70 (alpha neutral-95 0.7))
(def neutral-95-opa-80 (alpha neutral-95 0.8))
(def neutral-95-opa-90 (alpha neutral-95 0.9))
(def neutral-95-opa-95 (alpha neutral-95 0.95))

;;100 with transparency
(def neutral-100-opa-0 (alpha neutral-100 0))
(def neutral-100-opa-10 (alpha neutral-100 0.1))
(def neutral-100-opa-60 (alpha neutral-100 0.6))
(def neutral-100-opa-70 (alpha neutral-100 0.7))
(def neutral-100-opa-80 (alpha neutral-100 0.8))
(def neutral-100-opa-90 (alpha neutral-100 0.9))
(def neutral-100-opa-95 (alpha neutral-100 0.95))
(def neutral-100-opa-100 (alpha neutral-100 1))

;;80 with transparency opaque
(def neutral-80-opa-5-opaque (alpha-opaque neutral-80 0.05))

;;;;White

;;Solid
(def white "#ffffff")

;; with transparency
(def white-opa-5 (alpha white 0.05))
(def white-opa-10 (alpha white 0.1))
(def white-opa-20 (alpha white 0.2))
(def white-opa-30 (alpha white 0.3))
(def white-opa-40 (alpha white 0.4))
(def white-opa-50 (alpha white 0.5))
(def white-opa-60 (alpha white 0.6))
(def white-opa-70 (alpha white 0.7))
(def white-opa-80 (alpha white 0.8))
(def white-opa-90 (alpha white 0.9))
(def white-opa-95 (alpha white 0.95))

;;;;Black

;;Solid
(def black "#000000")

;;;;Primary

;;Solid
(def primary-50 "#4360DF")
(def primary-60 "#354DB2")

;;50 with transparency
(def primary-50-opa-5 (alpha primary-50 0.05))
(def primary-50-opa-10 (alpha primary-50 0.1))
(def primary-50-opa-20 (alpha primary-50 0.2))
(def primary-50-opa-30 (alpha primary-50 0.3))
(def primary-50-opa-40 (alpha primary-50 0.4))

;;;;Success

;;Solid
(def success-50 "#26A69A")
(def success-60 "#208B81")

;;50 with transparency
(def success-50-opa-5 (alpha success-50 0.05))
(def success-50-opa-10 (alpha success-50 0.1))
(def success-50-opa-20 (alpha success-50 0.2))
(def success-50-opa-30 (alpha success-50 0.3))
(def success-50-opa-40 (alpha success-50 0.4))

(def success-60-opa-5 (alpha success-60 0.05))
(def success-60-opa-10 (alpha success-60 0.1))
(def success-60-opa-20 (alpha success-60 0.2))
(def success-60-opa-30 (alpha success-60 0.3))
(def success-60-opa-40 (alpha success-60 0.4))

;;;;Danger

;;Solid
(def danger-50 "#E65F5C")
(def danger-60 "#C1504D")

;;50 with transparency
(def danger-50-opa-5 (alpha danger-50 0.05))
(def danger-50-opa-10 (alpha danger-50 0.1))
(def danger-50-opa-20 (alpha danger-50 0.2))
(def danger-50-opa-30 (alpha danger-50 0.3))
(def danger-50-opa-40 (alpha danger-50 0.4))

;;;; Customization

;; Colors for customizing profiles and communities themes
(def customization
  {:primary   {50 primary-50 ;; User can also use primary color as customisation color
               60 primary-60}
   :purple    {50 "#8661C1"
               60 "#5E478C"}
   :indigo    {50 "#496289"
               60 "#3D5273"}
   :turquoise {50 "#448EA2"
               60 "#397788"}
   :blue      {50 "#4CB4EF"
               60 "#4097C9"}
   :green     {50 "#5BCC95"
               60 "#4CAB7D"}
   :yellow    {50 "#FFCB53"
               60 "#D6AA46"}
   :orange    {50 "#FB8F61"
               60 "#D37851"}
   :red       {50 "#F46666"
               60 "#CD5656"}
   :pink      {50 "#FC7BAB"
               60 "#D46790"}
   :brown     {50 "#99604D"
               60 "#805141"}
   :beige     {50 "#CAAE93"
               60 "#AA927C"}})

(def colors-map
  (merge {:danger  {50 danger-50
                    60 danger-60}
          :success {50 success-50
                    60 success-60}}
         customization))

(def custom-color
  "(custom-color color suffix opacity)
   color   :primary/:purple/...
   suffix  50/60
   opacity 0-100 (optional)"
  (memoize
   (fn
     ([color suffix]
      (custom-color color suffix nil))
     ([color suffix opacity]
      (let [base-color (get-in colors-map [(keyword color) suffix])]
        (if opacity (alpha base-color (/ opacity 100)) base-color))))))

(defn custom-color-by-theme
  "(custom-color-by-theme color suffix-light suffix-dark opacity-light opacity-dark)
   color         :primary/:purple/...
   suffix-light  50/60
   suffix-dark   50/60
   opacity-light 0-100 (optional)
   opacity-dark  0-100 (optional)"
  ([color suffix-light suffix-dark]
   (custom-color-by-theme color suffix-light suffix-dark nil nil))
  ([color suffix-light suffix-dark opacity-light opacity-dark]
   (if (theme/dark?)
     (custom-color color suffix-dark opacity-dark)
     (custom-color color suffix-light opacity-light))))

(def shadow "rgba(9,16,28,0.04)")

;;General

;; divider
(def divider-light "#EDF2f4")
(def divider-dark "#0E1620")

(defn theme-colors
  "(theme-colors light dark override-theme)"
  ([light dark]
   (theme-colors light dark nil))
  ([light dark override-theme]
   (let [theme (or override-theme (theme/get-theme))]
     (if (= theme :light) light dark))))

(defn dark?
  []
  (theme/dark?))
