(ns test-helpers.matchers
  "Internal use. Don't require it directly."
  (:require
    [cljs.test :as test]
    [matcher-combinators.core :as core]
    [matcher-combinators.matchers :as matchers]
    [matcher-combinators.parser]
    [matcher-combinators.result :as result]))

;; This implementation is identical to `match?`, but wraps the expected value
;; with `nested-equals`. This differs from the default `embeds` matcher on maps,
;; where extra map keys are considered valid.
(defmethod test/assert-expr 'match-strict?
  [_ msg form]
  `(let [args#              (list ~@(rest form))
         [matcher# actual#] args#]
     (cond
       (not (= 2 (count args#)))
       (test/do-report
        {:type     :fail
         :message  ~msg
         :expected (symbol "`match-strict?` expects 2 arguments: a `matcher` and the `actual`")
         :actual   (symbol (str (count args#) " were provided: " '~form))})

       (core/matcher? matcher#)
       (let [result# (core/match (matchers/nested-equals matcher#) actual#)]
         (test/do-report
          (if (core/indicates-match? result#)
            {:type     :pass
             :message  ~msg
             :expected '~form
             :actual   (list 'match? matcher# actual#)}
            (with-file+line-info
             {:type     :fail
              :message  ~msg
              :expected '~form
              :actual   (tagged-for-pretty-printing (list '~'not (list 'match? matcher# actual#))
                                                    result#)
              :markup   (::result/value result#)}))))

       :else
       (test/do-report
        {:type     :fail
         :message  ~msg
         :expected (str "The first argument of `match-strict?` "
                        "needs to be a matcher (implement the match protocol)")
         :actual   '~form}))))
