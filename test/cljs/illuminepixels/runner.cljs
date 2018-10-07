(ns illuminepixels.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [illuminepixels.core-test]))

(doo-tests 'illuminepixels.core-test)
