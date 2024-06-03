(ns legacy.status-im.utils.styles (:refer-clojure :exclude [defn def]))

(defn- body
  [style]
  `(let [style#             ~style
         common#            (dissoc style# :android :ios)
         platform#          (keyword react-native.platform/os)
         platform-specific# (get style# platform#)]
     (if platform-specific#
       (merge common# platform-specific#)
       common#)))

(defmacro def
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

(defmacro defn
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
  `(clojure.core/defn ~style-name
     [~@params]
     ~(body style)))
