// fp:bab7097cf6cfaf861fd7719e6415a6b647b5d769a4b701008333345647fbccfa
#pragma once
#include <cstddef>
#include <cstdint>

constexpr char EXPECTED_CERT_PREFIX[] = "PLACEHOLDER_REPLACE_WITH_YOUR_CERT_SHA256";

static const uint8_t CLIENT_SECRET_SALT[16] = {0};
static constexpr size_t CLIENT_SECRET_TOTAL_LEN = 0;
static constexpr int CLIENT_SECRET_FRAGMENT_COUNT = 0;
struct Fragment { const uint8_t* data; uint8_t len; uint8_t mask; };
static const Fragment CLIENT_SECRET_FRAGMENTS[] = { {nullptr, 0, 0} };
