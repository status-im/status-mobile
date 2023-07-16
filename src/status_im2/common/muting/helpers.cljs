(ns status-im2.common.muting.helpers
  (:require [cljs-time.core :as t]
            [i18n-js :as i18n]
            [utils.datetime :refer
             [go-default-time int->weekday months
              time-zone-offset today? tomorrow?]])
  (:require [cljs-time.format :as t.format]))

(defn- add-leading-zero
  [input-string]
  (if (> 10 input-string)
    (str "0" input-string)
    input-string))

(defn format-mute-till
  [muted-till-string]
  (let [parsed-time       (t.format/parse (t.format/formatters :date-time-no-ms) muted-till-string)
        hours-and-minutes (str (add-leading-zero (t/hour (t/plus parsed-time time-zone-offset)))
                               ":"
                               (add-leading-zero (t/minute parsed-time)))
        when-to-unmute    (cond (= go-default-time
                                   muted-till-string)   (i18n/label :t/until-you-turn-it-back-on)
                                (today? parsed-time)    (str hours-and-minutes " today")
                                (tomorrow? parsed-time) (str hours-and-minutes " tomorrow")
                                :else                   (str hours-and-minutes
                                                             " "
                                                             (i18n/label
                                                              (keyword "t"
                                                                       (get int->weekday
                                                                            (t/day-of-week
                                                                             parsed-time))))
                                                             " "
                                                             (t/day parsed-time)
                                                             " "
                                                             (i18n/label
                                                              (keyword "t"
                                                                       (get months
                                                                            (t/month parsed-time))))))]
    when-to-unmute))
