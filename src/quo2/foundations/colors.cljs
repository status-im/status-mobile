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
