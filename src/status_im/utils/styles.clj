(ns status-im.utils.styles)

(def first-time (atom true))

(defn wrap-first-time
  "Allows to avoid
  \"Use of undeclared Var status-im.utils.platform/os\"
  warning. When defstyle or defnstyle is called first time status-im.utils.platform
  namespace will be explicitly required so that clojurescript compiler will compile
  it before using status-im.utils.platform/os in macro"
  [body]
  `(do
     ~@[(when @first-time
          (reset! first-time false)
          `(require 'status-im.utils.platform))]
     ~body))

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
  (wrap-first-time
   `(def ~style-name
      ~(body style))))

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
  (wrap-first-time
   `(defn ~style-name
      [~@params]
      ~(body style))))
