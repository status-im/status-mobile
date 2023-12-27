(ns quo.foundations.colors
  (:require
    [clojure.string :as string]
    [quo.theme :as theme]
    [react-native.platform :as platform]))

(def account-colors
  [:blue :yellow :purple :turquoise :magenta :sky :orange :army :flamingo :camel :copper])

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

(def neutral-2_5 "#FAFBFC")
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

(def neutral-50-opa-40 (alpha neutral-50 0.4))

;;80 with transparency
(def neutral-80-opa-5 (alpha neutral-80 0.05))
(def neutral-80-opa-10 (alpha neutral-80 0.1))
(def neutral-80-opa-15 (alpha neutral-80 0.15))
(def neutral-80-opa-20 (alpha neutral-80 0.2))
(def neutral-80-opa-30 (alpha neutral-80 0.3))
(def neutral-80-opa-40 (alpha neutral-80 0.4))
(def neutral-80-opa-50 (alpha neutral-80 0.5))
(def neutral-80-opa-60 (alpha neutral-80 0.6))
(def neutral-80-opa-70 (alpha neutral-80 0.7))
(def neutral-80-opa-80 (alpha neutral-80 0.8))
(def neutral-80-opa-90 (alpha neutral-80 0.9))
(def neutral-80-opa-95 (alpha neutral-80 0.95))

;;90 with transparency
(def neutral-90-opa-0 (alpha neutral-90 0))

;;95 with transparency
(def neutral-95-opa-0 (alpha neutral-95 0))
(def neutral-95-opa-60 (alpha neutral-95 0.6))
(def neutral-95-opa-70 (alpha neutral-95 0.7))
(def neutral-95-opa-80 (alpha neutral-95 0.8))
(def neutral-95-opa-90 (alpha neutral-95 0.9))
(def neutral-95-opa-95 (alpha neutral-95 0.95))

;;100 with transparency
(def neutral-100-opa-0 (alpha neutral-100 0))
(def neutral-100-opa-5 (alpha neutral-100 0.05))
(def neutral-100-opa-10 (alpha neutral-100 0.1))
(def neutral-100-opa-30 (alpha neutral-100 0.3))
(def neutral-100-opa-50 (alpha neutral-100 0.5))
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
(def white-opa-0 (alpha white 0))
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

;;;;Blur
(def white-70-blur (alpha white 0.7))
(def white-70-blur-opaque (alpha-opaque white 0.7))
(def neutral-80-opa-1-blur (alpha "#192438" 0.1))
(def neutral-5-opa-70-blur (alpha neutral-5 0.7))
(def neutral-10-opa-10-blur (alpha neutral-10 0.1))
(def neutral-10-opa-40-blur (alpha neutral-10 0.4))
;; https://github.com/status-im/status-mobile/issues/14903
(def neutral-80-opa-80-blur (if platform/android? (alpha "#192438" 0.8) "#1E2430CC"))
(def neutral-90-opa-10-blur (alpha neutral-90 0.1))
(def neutral-90-opa-40-blur (alpha neutral-90 0.4))
(def neutral-90-opa-70-blur (alpha neutral-90 0.7))
(def neutral-95-opa-70-blur neutral-95-opa-70)
(def neutral-100-opa-70-blur (if platform/android? neutral-100-opa-70 "#0D1014B3"))

;;;;Black

;;Solid
(def black "#000000")
(def black-opa-0 (alpha black 0))
(def black-opa-30 (alpha black 0.3))
(def black-opa-60 (alpha black 0.6))
(def onboarding-header-black "#000716")
(def border-avatar-light
  "Simulates a blurred, transparent border for avatars in light mode"
  "#475060")
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
(def success-50 "#23ADA0")
(def success-60 "#1C8A80")

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
(def danger "#E95460")

;; Danger with transparency
(def danger-opa-40 (alpha danger 0.4))

;;Solid
(def danger-50 "#E95460")
(def danger-60 "#BA434D")

(def system-yellow "#FFD60A")

;;50 with transparency
(def danger-50-opa-0 (alpha danger-50 0))
(def danger-50-opa-5 (alpha danger-50 0.05))
(def danger-50-opa-10 (alpha danger-50 0.1))
(def danger-50-opa-20 (alpha danger-50 0.2))
(def danger-50-opa-30 (alpha danger-50 0.3))
(def danger-50-opa-40 (alpha danger-50 0.4))

;;60 with transparency
(def danger-60-opa-10 (alpha danger-60 0.1))

;;;;Warning
(def warning-50 "#FF7D46")
(def warning-60 "#CC6438")

;;50 with transparency
(def warning-50-opa-5 (alpha warning-50 0.05))
(def warning-50-opa-10 (alpha warning-50 0.1))
(def warning-50-opa-20 (alpha warning-50 0.2))
(def warning-50-opa-30 (alpha warning-50 0.3))
(def warning-50-opa-40 (alpha warning-50 0.4))

;; Colors for customizing users account
(def customization
  {:blue      {50 "#2A4AF5"
               60 "#223BC4"}
   :yellow    {50 "#F6B03C"
               60 "#C58D30"}
   :turquoise {50 "#2A799B"
               60 "#22617C"}
   :copper    {50 "#CB6256"
               60 "#A24E45"}
   :sky       {50 "#1992D7"
               60 "#1475AC"}
   :camel     {50 "#C78F67"
               60 "#9F7252"}
   :orange    {50 "#FF7D46"
               60 "#CC6438"}
   :army      {50 "#216266"
               60 "#1A4E52"}
   :flamingo  {50 "#F66F8F"
               60 "#C55972"}
   :purple    {50 "#7140FD"
               60 "#5A33CA"}
   :magenta   {50 "#EC266C"
               60 "#BD1E56"}})

;;;; Networks
(def ^:private networks
  {:ethereum "#758EEB"
   :optimism "#E76E6E"
   :arbitrum "#6BD5F0"
   :zkSync   "#9FA0FE"
   :hermez   "#EB8462"
   :xDai     "#3FC0BD"
   :polygon  "#AD71F3"
   :unknown  "#EEF2F5"})

(def socials
  {:social/link      "#647084"
   :social/facebook  "#1877F2"
   :social/github    "#000000"
   :social/instagram "#D8408E0F"
   :social/lens      "#00501E"
   :social/linkedin  "#0B86CA"
   :social/mirror    "#3E7EF7"
   :social/opensea   "#2081E2"
   :social/pinterest "#CB2027"
   :social/rarible   "#FEDA03"
   :social/snapchat  "#FFFC00"
   :social/spotify   "#00DA5A"
   :social/superrare "#000000"
   :social/tumblr    "#37474F"
   :social/twitch    "#673AB7"
   :social/twitter   "#262E35"
   :social/youtube   "#FF3000"})

(def ^:private colors-map
  (merge {:primary {50 primary-50
                    60 primary-60}
          :beige   {50 "#CAAE93"
                    60 "#AA927C"}
          :green   {50 "#5BCC95"
                    60 "#4CAB7D"}
          :brown   {50 "#99604D"
                    60 "#805141"}
          :red     {50 "#F46666"
                    60 "#CD5656"}
          :indigo  {50 "#496289"
                    60 "#3D5273"}
          :danger  {50 danger-50
                    60 danger-60}
          :success {50 success-50
                    60 success-60}
          :warning {50 warning-50
                    60 warning-60}}
         customization
         networks
         socials))

(defn hex-string?
  [s]
  (and (string? s) (string/starts-with? s "#")))

(defn- get-from-colors-map
  [color suffix]
  (let [color-without-suffix (get colors-map color)
        resolved-color?      (hex-string? color-without-suffix)]
    (if resolved-color?
      color-without-suffix
      (get-in colors-map [color suffix]))))

(defn- resolve-color*
  ([color theme]
   (resolve-color* color theme nil))
  ([color theme opacity]
   (let [suffix (cond
                  (not (keyword? color))        nil
                  (or opacity (= theme :light)) 50
                  :else                         60)]
     (cond-> color
       suffix  (get-from-colors-map suffix)
       opacity (alpha (/ opacity 100))))))

(def resolve-color
  "(resolve-color color theme opacity)
   color   hex string or keyword (resolves from custom, network and semantic colors)
   theme  :light/:dark
   opacity 0-100 (optional) - if set theme is ignored and goes to 50 suffix internally"
  (memoize resolve-color*))

(def ^{:deprecated true :superseded-by "resolve-color"}
     custom-color
  "(custom-color color suffix opacity)
   color   :primary/:purple/...
   suffix  50/60
   opacity 0-100 (optional)"
  (memoize
   (fn
     ([color]
      (custom-color color nil nil))
     ([color suffix]
      (custom-color color suffix nil))
     ([color suffix opacity]
      (let [hex?           (not (keyword? color))
            resolved-color (cond hex?                                 color
                                 (hex-string? (get colors-map color)) (get colors-map color)
                                 :else                                (get-in colors-map
                                                                              [color suffix]))]
        (if opacity
          (alpha resolved-color (/ opacity 100))
          resolved-color))))))

(def shadow "rgba(9,16,28,0.08)")

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
