(ns quo2.foundations.colors
  (:require [clojure.string :as string]
            [quo.theme :as theme]))

(defn alpha [value opacity]
  (if (string/starts-with? value "#")
    (let [hex (string/replace value #"#" "")
          r   (js/parseInt (subs hex 0 2) 16)
          g   (js/parseInt (subs hex 2 4) 16)
          b   (js/parseInt (subs hex 4 6) 16)]
      (str "rgba(" r "," g "," b "," opacity ")"))
    (let [rgb (string/split value #",")]
      (str (string/join "," (butlast rgb)) "," opacity ")"))))

;;;;Neutral

;;Solid
(def neutral-5 "#F5F6F8")
(def neutral-10 "#F0F2F5")
(def neutral-20 "#ECEEF1")
(def neutral-30 "#E7EAEE")
(def neutral-40 "#A1ABBD")
(def neutral-50 "#647084")
(def neutral-60 "#303D55")
(def neutral-70 "#192438")
(def neutral-80 "#131D2F")
(def neutral-90 "#0D1625")
(def neutral-95 "#09101C")

;;50 with transparency
(def neutral-50-opa-5 (alpha neutral-50 0.05))
(def neutral-50-opa-10 (alpha neutral-50 0.1))
(def neutral-50-opa-20 (alpha neutral-50 0.2))
(def neutral-50-opa-30 (alpha neutral-50 0.3))
(def neutral-50-opa-40 (alpha neutral-50 0.4))

;;70 with transparency
(def neutral-70-opa-60 (alpha neutral-70 0.6))
(def neutral-70-opa-70 (alpha neutral-70 0.7))
(def neutral-70-opa-80 (alpha neutral-70 0.8))
(def neutral-70-opa-90 (alpha neutral-70 0.9))
(def neutral-70-opa-95 (alpha neutral-70 0.95))

;;80 with transparency
(def neutral-80-opa-5  (alpha neutral-80 0.05))
(def neutral-80-opa-10 (alpha neutral-80 0.1))
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

;;;;Black
(def black "#000000")
(def black-opa-5 (alpha black 0.05))
(def black-opa-10 (alpha black 0.1))
(def black-opa-20 (alpha black 0.2))
(def black-opa-30 (alpha black 0.3))
(def black-opa-40 (alpha black 0.4))
(def black-opa-50 (alpha black 0.5))
(def black-opa-60 (alpha black 0.6))
(def black-opa-70 (alpha black 0.7))
(def black-opa-80 (alpha black 0.8))
(def black-opa-90 (alpha black 0.9))
(def black-opa-95 (alpha black 0.95))

;;;;White
(def white "#ffffff")
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

;;;;Primary

;;Solid
(def primary-10 "#F8F9FE")
(def primary-20 "#D9DFF9")
(def primary-30 "#ACB9F1")
(def primary-40 "#7F93E9")
(def primary-50 "#4360DF")
(def primary-60 "#3851BB")
(def primary-70 "#2E4198")
(def primary-80 "#233274")
(def primary-90 "#182350")

;;50 with transparency
(def primary-50-opa-5 (alpha primary-50 0.05))
(def primary-50-opa-10 (alpha primary-50 0.1))
(def primary-50-opa-20 (alpha primary-50 0.2))
(def primary-50-opa-30 (alpha primary-50 0.3))
(def primary-50-opa-40 (alpha primary-50 0.4))

;;60 with transparency
(def primary-60-opa-5 (alpha primary-60 0.05))

;;;;Success

;;Solid
(def success-10 "#F6FBFB")
(def success-20 "#D4EDEB")
(def success-30 "#9FD8D3")
(def success-40 "#6BC2BA")
(def success-50 "#26A69A")
(def success-60 "#208B81")
(def success-70 "#1A7169")
(def success-80 "#145650")
(def success-90 "#08211F")

;;50 with transparency
(def success-50-opa-5 (alpha success-50 0.05))
(def success-50-opa-10 (alpha success-50 0.1))
(def success-50-opa-20 (alpha success-50 0.2))
(def success-50-opa-30 (alpha success-50 0.3))
(def success-50-opa-40 (alpha success-50 0.4))

;;;;Warning

;;Solid
(def warning-10 "#FFFBF9")
(def warning-20 "#FEE9DF")
(def warning-30 "#FDCEBA")
(def warning-40 "#FCB394")
(def warning-50 "#FB8F61")
(def warning-60 "#D37851")
(def warning-70 "#AB6142")
(def warning-80 "#824A32")
(def warning-90 "#5A3323")

;;50 with transparency
(def warning-50-opa-5 (alpha warning-50 0.05))
(def warning-50-opa-10 (alpha warning-50 0.1))
(def warning-50-opa-20 (alpha warning-50 0.2))
(def warning-50-opa-30 (alpha warning-50 0.3))
(def warning-50-opa-40 (alpha warning-50 0.4))

;;;;Danger

;;Solid
(def danger-10 "#FEF9F9")
(def danger-20 "#FADFDE")
(def danger-30 "#F4B9B7")
(def danger-40 "#EE9290")
(def danger-50 "#E65F5C")
(def danger-60 "#C1504D")
(def danger-70 "#9C413F")
(def danger-80 "#783130")
(def danger-90 "#532221")

;;50 with transparency
(def danger-50-opa-5 (alpha danger-50 0.05))
(def danger-50-opa-10 (alpha danger-50 0.1))
(def danger-50-opa-20 (alpha danger-50 0.2))
(def danger-50-opa-30 (alpha danger-50 0.3))
(def danger-50-opa-40 (alpha danger-50 0.4))

;;;;Info

;;Solid
(def info-10 "#F6FCFD")
(def info-20 "#D2EEF4")
(def info-30 "#9DD9E6")
(def info-40 "#67C4D8")
(def info-50 "#20A8C6")
(def info-60 "#1B8DA6")
(def info-70 "#167287")
(def info-80 "#115767")
(def info-90 "#0C3C47")

;;50 with transparency
(def info-50-opa-5 (alpha info-50 0.05))
(def info-50-opa-10 (alpha info-50 0.1))
(def info-50-opa-20 (alpha info-50 0.2))
(def info-50-opa-30 (alpha info-50 0.3))
(def info-50-opa-40 (alpha info-50 0.4))

;; Customization
;;;;Purple
(def purple-20 (alpha "#8661C1" 0.2))
(def purple-50 "#8661C1")
(def purple-60 "#5E478C")

;;with transparency
(def purple-50-opa-5 (alpha purple-50 0.05))
(def purple-60-opa-5 (alpha purple-60 0.05))

;;;;Indigo
(def indigo-20 (alpha "#496289" 0.2))
(def indigo-50 "#496289")
(def indigo-60 "#3D5273")

;;with transparency
(def indigo-50-opa-5 (alpha indigo-50 0.05))
(def indigo-60-opa-5 (alpha indigo-60 0.05))

;;;;Turquoise
(def turquoise-20 (alpha "#448EA2" 0.2))
(def turquoise-50 "#448EA2")
(def turquoise-60 "#397788")

;;with transparency
(def turquoise-50-opa-5 (alpha turquoise-50 0.05))
(def turquoise-60-opa-5 (alpha turquoise-60 0.05))

;;;;Blue
(def blue-20 (alpha "#4CB4EF" 0.2))
(def blue-50 "#4CB4EF")
(def blue-60 "#4097C9")

;;with transparency
(def blue-50-opa-5 (alpha blue-50 0.05))
(def blue-60-opa-5 (alpha blue-60 0.05))

;;;;Green
(def green-20 (alpha "#5BCC95" 0.2))
(def green-50 "#5BCC95")
(def green-60 "#4CAB7D")

;;with transparency
(def green-50-opa-5 (alpha green-50 0.05))
(def green-60-opa-5 (alpha green-60 0.05))

;;;;Yellow
(def yellow-20 (alpha "#FFCB53" 0.2))
(def yellow-50 "#FFCB53")
(def yellow-60 "#D6AA46")

;;with transparency
(def yellow-50-opa-5 (alpha yellow-50 0.05))
(def yellow-60-opa-5 (alpha yellow-60 0.05))

;;;;Orange
(def orange-20 (alpha "#FB8F61" 0.2))
(def orange-50 "#FB8F61")
(def orange-60 "#D37851")

;;with transparency
(def orange-50-opa-5 (alpha orange-50 0.05))
(def orange-60-opa-5 (alpha orange-60 0.05))

;;;;Red
(def red-20 (alpha "#F46666" 0.2))
(def red-50 "#F46666")
(def red-60 "#CD5656")

;;with transparency
(def red-50-opa-5 (alpha red-50 0.05))
(def red-60-opa-5 (alpha red-60 0.05))

;;;;Pink
(def pink-20 (alpha "#FC7BAB" 0.2))
(def pink-50 "#FC7BAB")
(def pink-60 "#D46790")

;;with transparency
(def pink-50-opa-5 (alpha pink-50 0.05))
(def pink-60-opa-5 (alpha pink-60 0.05))

;;;;Brown
(def brown-20 (alpha "#99604D" 0.2))
(def brown-50 "#99604D")
(def brown-60 "#805141")

;;with transparency
(def brown-50-opa-5 (alpha brown-50 0.05))
(def brown-60-opa-5 (alpha brown-60 0.05))

;;;;Beige
(def beige-20 (alpha "#CAAE93" 0.2))
(def beige-50 "#CAAE93")
(def beige-60 "#AA927C")

;;with transparency
(def beige-50-opa-5 (alpha beige-50 0.05))
(def beige-60-opa-5 (alpha beige-60 0.05))

(def shadow  "rgba(9,16,28,0.04)")

(def customization
  {:dark {:purple purple-60
          :indigo indigo-60
          :turquoise turquoise-60
          :blue blue-60
          :green green-60
          :yellow yellow-60
          :orange orange-60
          :red red-60
          :pink pink-60
          :brown brown-60
          :beige beige-60}
   :light {:purple purple-50
           :indigo indigo-50
           :turquoise turquoise-50
           :blue blue-50
           :green green-50
           :yellow yellow-50
           :orange orange-50
           :red red-50
           :pink pink-50
           :brown brown-50
           :beige beige-50}})

(defn custom-color [color theme]
  (get-in customization [theme color]))

;;;;Switcher

(def switcher-background "#040B14")

;;switcher-screen with transparency
(def switcher-background-opa-60 (alpha switcher-background 0.6))
(def switcher-background-opa-70 (alpha switcher-background 0.7))
(def switcher-background-opa-80 (alpha switcher-background 0.8))
(def switcher-background-opa-90 (alpha switcher-background 0.9))
(def switcher-background-opa-95 (alpha switcher-background 0.95))

;;General

;; background

(def ui-background-02-light "#F5F9FA")

;; divider
(def divider-light "#EDF2f4")
(def divider-dark "#0E1620")

;; Visibility status

(def color-online "#26A69A")

(defn theme-colors [light dark]
  (if (theme/dark?) dark light))

(defn dark?
  []
  (theme/dark?))
