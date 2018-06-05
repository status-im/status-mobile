(ns status-im.ui.components.colors
  (:require [clojure.string :as string]))

(def white "#ffffff")
(def white-light-transparent "rgba(255, 255, 255, 0.1)") ;; Used as icon background color for a dark foreground
(def white-transparent "rgba(255, 255, 255, 0.2)") ;; Used as icon color on dark background
(def white-lighter-transparent "rgba(255, 255, 255, 0.6)") ;; Used for input placeholder color
(def black "#000000") ;; Used as the default text color
(def black-transparent "#00000020") ;; Used as background color for rounded button on dark background
(def black-darker-transparent "#00000033") ;; Used as background color for containers like "Backup seed phrase"
(def gray "#939ba1") ;; Used as a background for a light foreground and as section header and secondary text color
(def gray-icon "#6e777e") ;; Used for forward icon in accounts
(def gray-light "#e8ebec") ;; Used as divider color
(def gray-lighter "#eef2f5")  ;; Used as a background or shadow
(def gray-transparent "rgba(184, 193, 199, 0.5)") ;; Used for tabs
(def gray-notifications "#4A5054cc") ;; Used for notifications background
(def gray-border "#ececf0")
(def blue "#4360df") ;; Used as main wallet color, and ios home add button
(def blue-dark "#3147ac") ;; Used as secondary wallet color (icon background)
(def hawkes-blue "#dce2fb") ;; Outgoing chat messages background
(def wild-blue-yonder "#707caf") ;; Text color for outgoing messages timestamp
(def red "#ff2d55") ;; Used to highlight errors or "dangerous" actions
(def red-light "#ffe5ea") ;; error tooltip
(def text-light-gray "#212121") ;; Used for labels (home items)
(def cyan "#7adcfb") ;; Used by wallet transaction filtering icon
(def photo-border-color "#ccd3d6")

(def chat-colors ["#fa6565"
                  "#7cda00"
                  "#887af9"
                  "#51d0f0"
                  "#fe8f59"
                  "#d37ef4"])

(defn alpha [hex opacity]
  (let [hex (string/replace hex #"#" "")
        r (js/parseInt (subs hex 0 2) 16)
        g (js/parseInt (subs hex 2 4) 16)
        b (js/parseInt (subs hex 4 6) 16)]
    (str "rgba(" r "," g "," b "," opacity ")")))

(def text black)
