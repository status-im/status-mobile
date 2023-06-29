(ns quo2.foundations.resources)

(def ui
  {:keycard-logo       (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark  (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-watermark  (js/require "../resources/images/ui2/keycard-watermark.png")})

(defn get-image
  [k]
  (get ui k))
