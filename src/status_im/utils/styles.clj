(ns status-im.utils.styles)

(defn body [styles]
  `(let [styles#            ~styles
         common#            (dissoc styles# :android :ios)
         platform#          (keyword status-im.utils.platform/platform)
         platform-specific# (get styles# platform#)]
     (if platform-specific#
       (merge common# platform-specific#)
       common#)))

(defmacro defstyles
  "Defines styles symbol.
   Styles parameter may contain plaform specific styles:
   {:width   100
    :height  125
    :ios     {:height 20}
    :android {:margin-top 3}}

    Reuslting styles for Android:
    {:width 100
     :height 125
     :margin-top 3}

    Resulting styles for iOS:
    {:width  100
     :height 20}"
  [style-name styles]
  `(def ~style-name
     ~(body styles)))

(defmacro defnstyles
  "Defines styles function.
   Styles parameter may contain plaform specific styles:
   {:width   100
    :height  (* a 2)
    :ios     {:height (/ a 2)}
    :android {:margin-top 3}}

    Resulting styles for Android (with (= a 10)):
    {:width 100
     :height 20
     :margin-top 3}

    Resulting styles for iOS (with (= a 10)):
    {:width  100
     :height 5}"
  [style-name params styles]
  `(defn ~style-name
     [~@params]
     ~(body styles)))
