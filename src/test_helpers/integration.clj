(ns test-helpers.integration
  (:require [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]))

(defmacro with-app-initialized
  [& body]
  `(do
     (legacy.status-im.utils.test/init!)
     (if (test-helpers.integration/app-initialized)
       (do ~@body)
       (do
         (rf/dispatch [:app-started])
         (rf-test/wait-for [:profile/get-profiles-overview-success]
           ~@body)))))

(defmacro with-account
  [& body]
  `(if (test-helpers.integration/messenger-started)
     (do ~@body)
     (do
       (test-helpers.integration/create-multiaccount!)
       (rf-test/wait-for [:messenger-started]
         (test-helpers.integration/assert-messenger-started)
         ~@body))))
