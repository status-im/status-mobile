(ns status-im.utils.styles)

(defn body [style]
  `(let [style#            ~style
         common#            (dissoc style# :android :ios :desktop)
         platform#          (keyword status-im.utils.platform/os)
         platform-specific# (get style# platform#)]
     (if platform-specific#
       (merge common# platform-specific#)
       common#)))

(defmacro defstyle
  "Defines style symbol.
   Style parameter may contain platform specific style:
   {:width   100
    :height  125
    :ios     {:height 20}
    :android {:margin-top 3}}

    Resulting style for Android:
    {:width 100
     :height 125
     :margin-top 3}

    Resulting style for iOS:
    {:width  100
     :height 20}"
  [style-name style]
  `(def ~style-name
     ~(body style)))

(defmacro defnstyle
  "Defines style function.
   Style parameter may contain platform specific style:
   {:width   100
    :height  (* a 2)
    :ios     {:height (/ a 2)}
    :android {:margin-top 3}}

    Resulting style for Android (with (= a 10)):
    {:width 100
     :height 20
     :margin-top 3}

    Resulting style for iOS (with (= a 10)):
    {:width  100
     :height 5}"
  [style-name params style]
  `(defn ~style-name
     [~@params]
     ~(body style)))
