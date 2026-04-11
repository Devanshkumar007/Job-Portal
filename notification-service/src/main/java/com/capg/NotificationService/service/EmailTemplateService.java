package com.capg.NotificationService.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private static final String PRIMARY = "#2563eb";
    private static final String PRIMARY_DARK = "#1d4ed8";
    private static final String BG = "#f5f7fa";
    private static final String SURFACE = "#ffffff";
    private static final String SURFACE2 = "#f8fbff";
    private static final String BORDER = "#dbe3ee";
    private static final String TEXT = "#0f172a";
    private static final String TEXT2 = "#64748b";
    private static final String ACCENT = "#10b981";

    private String wrap(String previewText, String content) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>Nexus Careers</title></head>" +
                "<body style='margin:0;padding:0;background:" + BG + ";font-family:Arial,Helvetica,sans-serif'>" +
                "<div style='display:none;max-height:0;overflow:hidden;mso-hide:all'>" + previewText + "</div>" +
                "<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:" + BG + ";min-height:100vh'>" +
                "<tr><td align='center' style='padding:32px 16px'>" +
                "<table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;width:100%'>" +

                // Header
                "<tr><td style='background:" + SURFACE + ";border-radius:20px 20px 0 0;border:1px solid " + BORDER + ";border-bottom:none;padding:28px 36px'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>" +
                "<td>" +
                "<span style='display:inline-block;width:40px;height:40px;background:" + PRIMARY + ";border-radius:12px;font-weight:900;font-size:14px;color:#fff;text-align:center;line-height:40px;vertical-align:middle'>NC</span>" +
                "<span style='display:inline-block;font-size:18px;font-weight:800;color:" + TEXT + ";vertical-align:middle;margin-left:10px;letter-spacing:-0.03em'>Nexus Careers</span>" +
                "</td>" +
                "<td align='right'><span style='font-size:11px;color:" + TEXT2 + ";letter-spacing:0.12em;text-transform:uppercase'>Career Platform</span></td>" +
                "</tr></table></td></tr>" +

                // Content
                "<tr><td style='background:" + SURFACE + ";border:1px solid " + BORDER + ";border-top:none;border-bottom:none;padding:0 36px 32px'>" +
                content +
                "</td></tr>" +

                // Footer
                "<tr><td style='background:" + SURFACE2 + ";border-radius:0 0 20px 20px;border:1px solid " + BORDER + ";border-top:1px solid " + BORDER + ";padding:24px 36px;text-align:center'>" +
                "<p style='margin:0 0 8px;font-size:12px;color:" + TEXT2 + "'>You're receiving this because you have an account on Nexus Careers.</p>" +
                "<p style='margin:0;font-size:11px;color:" + TEXT2 + "'>© 2026 Nexus Careers · Privacy Policy</p>" +
                "</td></tr>" +

                "</table></td></tr></table></body></html>";
    }

    private String divider() {
        return "<div style='height:1px;background:" + BORDER + ";margin:24px 0'></div>";
    }

    private String badge(String text, String color, String bg) {
        return "<span style='display:inline-block;padding:4px 12px;border-radius:999px;background:" + bg + ";color:" + color + ";font-size:11px;font-weight:700;letter-spacing:0.08em;text-transform:uppercase;border:1px solid " + color + "33'>" + text + "</span>";
    }

    private String factRow(String label, String value) {
        return "<tr>" +
                "<td style='padding:10px 0;font-size:13px;color:" + TEXT2 + ";width:40%'>" + label + "</td>" +
                "<td style='padding:10px 0;font-size:13px;font-weight:600;color:" + TEXT + ";text-align:right'>" + value + "</td>" +
                "</tr>";
    }

    // ─── TEMPLATE 1: Application Received ───────────────────────────────────────
    public String applicationCreated(String jobTitle, String company, String status) {
        String content =
                "<div style='padding:32px 0 8px'>" +
                        "<div style='width:56px;height:56px;background:rgba(16,185,129,0.12);border:1px solid rgba(16,185,129,0.25);border-radius:16px;text-align:center;line-height:56px;font-size:26px;margin-bottom:20px'>✅</div>" +
                        "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.03em'>Application received!</h1>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>Great news — your application has been submitted and is now in the recruiter's queue. We'll keep you posted on every update.</p>" +
                        "</div>" +

                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:20px 24px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 14px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + TEXT2 + "'>Application details</p>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        factRow("Job title", jobTitle) +
                        factRow("Company", company) +
                        factRow("Status", "<span style='color:" + PRIMARY + ";font-weight:700'>" + status + "</span>") +
                        "</table></div>" +

                        "<div style='background:rgba(16,185,129,0.06);border:1px solid rgba(16,185,129,0.20);border-radius:14px;padding:16px 20px;margin-bottom:24px'>" +
                        "<p style='margin:0;font-size:13px;color:" + PRIMARY + ";line-height:1.6'><strong>What's next?</strong> The recruiter will review your application and update the status. You'll receive an email at every stage.</p>" +
                        "</div>";

        return wrap("Your application for " + jobTitle + " at " + company + " has been received.", content);
    }

    // ─── TEMPLATE 2: Application Status Update ──────────────────────────────────
    public String applicationStatusUpdate(String jobTitle, String company, String status) {
        boolean isShortlisted = "SHORTLISTED".equalsIgnoreCase(status);
        boolean isRejected = "REJECTED".equalsIgnoreCase(status);

        String iconEmoji = isShortlisted ? "🌟" : isRejected ? "📋" : "🔄";
        String accentColor = isShortlisted ? ACCENT : isRejected ? "#ef4444" : PRIMARY;
        String headline = isShortlisted ? "You've been shortlisted!" : isRejected ? "Application update" : "Status updated";
        String message = isShortlisted
                ? "Excellent news — the recruiter has shortlisted your application. Expect to hear about next steps soon."
                : isRejected
                ? "Thank you for applying. After careful review, the recruiter decided to move forward with other candidates. Don't be discouraged — keep applying!"
                : "Your application status has been updated. Log in to your dashboard for full details.";
        String journeySection = isRejected
                ? ""
                : "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:20px 24px;margin-bottom:24px'>" +
                  "<p style='margin:0 0 16px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + TEXT2 + "'>Application journey</p>" +
                  "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>" +
                  timelineStep("Applied", true, PRIMARY) + timelineArrow() +
                  timelineStep("Under Review", !isRejected, PRIMARY) + timelineArrow() +
                  timelineStep("Shortlisted", isShortlisted, PRIMARY) +
                  "</tr></table></div>";

        String content =
                "<div style='padding:32px 0 8px'>" +
                        "<div style='width:56px;height:56px;background:" + accentColor + "1a;border:1px solid " + accentColor + "40;border-radius:16px;text-align:center;line-height:56px;font-size:26px;margin-bottom:20px'>" + iconEmoji + "</div>" +
                        "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.03em'>" + headline + "</h1>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>" + message + "</p>" +
                        "</div>" +

                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:20px 24px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 14px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + TEXT2 + "'>Application details</p>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        factRow("Job title", jobTitle) +
                        factRow("Company", company) +
                        factRow("New status", "<span style='color:" + accentColor + ";font-weight:700'>" + status + "</span>") +
                        "</table></div>" +
                        journeySection;

        return wrap("Your application for " + jobTitle + " is now: " + status, content);
    }

    private String timelineStep(String label, boolean done, String color) {
        String bg = done ? color : SURFACE;
        String textColor = done ? "#fff" : TEXT2;
        String border = done ? color : BORDER;
        return "<td style='text-align:center;padding:0 4px'>" +
                "<div style='width:32px;height:32px;border-radius:50%;background:" + bg + ";border:2px solid " + border + ";margin:0 auto 6px;line-height:32px;font-size:12px;color:" + textColor + ";font-weight:700'>" +
                (done ? "✓" : "·") + "</div>" +
                "<div style='font-size:10px;color:" + (done ? color : TEXT2) + ";font-weight:" + (done ? "700" : "400") + ";white-space:nowrap'>" + label + "</div>" +
                "</td>";
    }

    private String timelineArrow() {
        return "<td style='text-align:center;padding:0;padding-bottom:22px'>" +
                "<div style='height:2px;background:" + BORDER + ";margin:0 4px;margin-top:15px'></div>" +
                "</td>";
    }

    // ─── TEMPLATE 3: Job Posted ──────────────────────────────────────────────────
    public String jobCreated(String jobTitle) {
        String content =
                "<div style='padding:32px 0 8px'>" +
                        "<div style='width:56px;height:56px;background:rgba(16,185,129,0.12);border:1px solid rgba(16,185,129,0.25);border-radius:16px;text-align:center;line-height:56px;font-size:26px;margin-bottom:20px'>🚀</div>" +
                        "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.03em'>Your job is live!</h1>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>Your job posting has been published and is now visible to thousands of job seekers on Nexus Careers. Applications will start coming in shortly.</p>" +
                        "</div>" +

                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:20px 24px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 14px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + TEXT2 + "'>Job details</p>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        factRow("Job title", jobTitle) +
                        factRow("Status", "<span style='color:" + ACCENT + ";font-weight:700'>Live ✦</span>") +
                        "</table></div>" +

                        "<div style='display:table;width:100%;border-collapse:separate;border-spacing:0'>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>" +
                        tipCard("🔍", "Boost visibility", "Share your job link on LinkedIn and social media to reach more candidates.") +
                        "<td width='12'></td>" +
                        tipCard("⚡", "Quick responses", "Responding to applicants quickly improves your offer acceptance rate.") +
                        "</tr></table></div>";

        return wrap("Your job posting '" + jobTitle + "' is now live on Nexus Careers.", content);
    }

    private String tipCard(String icon, String title, String text) {
        return "<td style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:14px;padding:16px;vertical-align:top;width:50%'>" +
                "<div style='font-size:20px;margin-bottom:8px'>" + icon + "</div>" +
                "<div style='font-size:13px;font-weight:700;color:" + TEXT + ";margin-bottom:6px'>" + title + "</div>" +
                "<div style='font-size:12px;color:" + TEXT2 + ";line-height:1.6'>" + text + "</div>" +
                "</td>";
    }

    // ─── TEMPLATE 4: Interview Scheduled ────────────────────────────────────────
    public String interviewScheduled(String name, String jobTitle, String company,
                                     String date, String time, String timezone, String link) {
        String content =
                "<div style='padding:32px 0 8px'>" +
                        "<div style='width:56px;height:56px;background:rgba(37,99,235,0.12);border:1px solid rgba(37,99,235,0.25);border-radius:16px;text-align:center;line-height:56px;font-size:26px;margin-bottom:20px'>📅</div>" +
                        "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.03em'>Interview scheduled</h1>" +
                        "<p style='margin:0 0 8px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>Hi <strong style='color:" + TEXT + "'>" + name + "</strong>,</p>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>Your interview has been confirmed. Please review the details below and join on time. Good luck!</p>" +
                        "</div>" +

                        // Interview card
                        "<div style='background:rgba(37,99,235,0.06);border:1px solid rgba(37,99,235,0.20);border-radius:16px;padding:24px;margin-bottom:20px'>" +
                        "<p style='margin:0 0 16px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + PRIMARY_DARK + "'>Interview details</p>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        "<tr><td style='padding:10px 0;font-size:13px;color:" + TEXT2 + ";width:40%'>Role</td><td style='padding:10px 0;font-size:13px;font-weight:600;color:" + TEXT + ";text-align:right'>" + jobTitle + "</td></tr>" +
                        "<tr><td style='padding:10px 0;font-size:13px;color:" + TEXT2 + "'>Company</td><td style='padding:10px 0;font-size:13px;font-weight:600;color:" + TEXT + ";text-align:right'>" + company + "</td></tr>" +
                        "<tr><td style='padding:10px 0;font-size:13px;color:" + TEXT2 + "'>Date</td><td style='padding:10px 0;font-size:14px;font-weight:700;color:" + PRIMARY_DARK + ";text-align:right'>" + date + "</td></tr>" +
                        "<tr><td style='padding:10px 0;font-size:13px;color:" + TEXT2 + "'>Time</td><td style='padding:10px 0;font-size:14px;font-weight:700;color:" + PRIMARY_DARK + ";text-align:right'>" + time + " (" + timezone + ")</td></tr>" +
                        "<tr><td style='padding:10px 0;font-size:13px;color:" + TEXT2 + "'>Meeting link</td><td style='padding:10px 0;font-size:13px;font-weight:600;text-align:right'><a href='" + link + "' style='color:" + PRIMARY + ";text-decoration:none'>Open meeting link</a></td></tr>" +
                        "</table></div>" +

                        // Prep tips
                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:18px 20px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 12px;font-size:12px;font-weight:700;color:" + TEXT2 + ";text-transform:uppercase;letter-spacing:0.12em'>Quick prep checklist</p>" +
                        checkItem("Test your mic and camera before joining") +
                        checkItem("Research " + company + "'s recent work and values") +
                        checkItem("Prepare 2-3 questions to ask the interviewer") +
                        checkItem("Keep your resume accessible during the call") +
                        "</div>" +

                        "<div style='text-align:center;margin:4px 0 0'>" +
                        "<a href='" + link + "' style='display:inline-block;padding:14px 34px;background:" + PRIMARY + ";color:#fff;font-size:14px;font-weight:800;text-decoration:none;border-radius:12px'>Join call</a>" +
                        "</div>";

        return wrap("Your interview for " + jobTitle + " at " + company + " is confirmed.", content);
    }

    private String checkItem(String text) {
        return "<div style='display:flex;align-items:center;margin-bottom:8px'>" +
                "<span style='width:18px;height:18px;border-radius:6px;background:rgba(16,185,129,0.15);border:1px solid rgba(16,185,129,0.25);display:inline-block;text-align:center;line-height:18px;font-size:10px;color:" + ACCENT + ";margin-right:10px;flex-shrink:0'>✓</span>" +
                "<span style='font-size:13px;color:" + TEXT2 + "'>" + text + "</span>" +
                "</div>";
    }

    // ─── TEMPLATE 5: Offer Sent ──────────────────────────────────────────────────
    public String offerSent(String name, String jobTitle, String company,
                            String status, String offerLetterUrl) {
        String content =
                // Big confetti-style header
                "<div style='background:linear-gradient(135deg,rgba(37,99,235,0.10),rgba(16,185,129,0.10));border:1px solid rgba(37,99,235,0.20);border-radius:18px;padding:32px 28px;text-align:center;margin:24px 0'>" +
                        "<div style='font-size:40px;margin-bottom:14px'>🎉</div>" +
                        "<h1 style='margin:0 0 10px;font-size:28px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.04em'>Congratulations, " + name + "!</h1>" +
                        "<p style='margin:0;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>You've received an offer for <strong style='color:" + PRIMARY + "'>" + jobTitle + "</strong> at <strong style='color:" + TEXT + "'>" + company + "</strong>. This is a big moment - you earned it.</p>" +
                        "</div>" +

                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:20px 24px;margin-bottom:20px'>" +
                        "<p style='margin:0 0 14px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.14em;color:" + TEXT2 + "'>Offer summary</p>" +
                        "<table width='100%' cellpadding='0' cellspacing='0' border='0'>" +
                        factRow("Role", jobTitle) +
                        factRow("Company", company) +
                        factRow("Offer status", "<span style='color:" + PRIMARY + ";font-weight:700'>" + status + "</span>") +
                        "</table></div>" +

                        "<div style='background:rgba(16,185,129,0.05);border:1px solid rgba(16,185,129,0.18);border-radius:14px;padding:16px 20px;margin-bottom:24px'>" +
                        "<p style='margin:0 0 6px;font-size:13px;font-weight:700;color:" + PRIMARY + "'>Next steps</p>" +
                        "<p style='margin:0;font-size:13px;color:" + TEXT2 + ";line-height:1.7'>Review your offer letter carefully. If you have questions, reach out to the recruiter directly through your Nexus Careers dashboard before accepting.</p>" +
                        "</div>" +

                        "<div style='text-align:center;margin:4px 0 0'>" +
                        "<a href='" + offerLetterUrl + "' style='display:inline-block;padding:14px 34px;background:" + PRIMARY + ";color:#fff;font-size:14px;font-weight:800;text-decoration:none;border-radius:12px'>View offer letter</a>" +
                        "</div>";

        return wrap("You have an offer for " + jobTitle + " at " + company + "!", content);
    }

    // ─── TEMPLATE 6: Forgot Password ─────────────────────────────────────────────
    public String forgotPassword(String name, String resetLink) {
        String content =
                "<div style='padding:32px 0 8px'>" +
                        "<div style='width:56px;height:56px;background:rgba(37,99,235,0.12);border:1px solid rgba(37,99,235,0.25);border-radius:16px;text-align:center;line-height:56px;font-size:26px;margin-bottom:20px'>🔐</div>" +
                        "<h1 style='margin:0 0 10px;font-size:26px;font-weight:800;color:" + TEXT + ";letter-spacing:-0.03em'>Reset your password</h1>" +
                        "<p style='margin:0 0 8px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>Hi <strong style='color:" + TEXT + "'>" + name + "</strong>,</p>" +
                        "<p style='margin:0 0 24px;font-size:15px;color:" + TEXT2 + ";line-height:1.7'>We received a request to reset your password. Use the button below to set a new one.</p>" +
                        "</div>" +
                        "<div style='text-align:center;margin:4px 0 24px'>" +
                        "<a href='" + resetLink + "' style='display:inline-block;padding:14px 34px;background:" + PRIMARY + ";color:#fff;font-size:14px;font-weight:800;text-decoration:none;border-radius:12px'>Reset password</a>" +
                        "</div>" +
                        "<div style='background:" + SURFACE2 + ";border:1px solid " + BORDER + ";border-radius:16px;padding:18px 20px'>" +
                        "<p style='margin:0 0 8px;font-size:13px;color:" + TEXT2 + ";line-height:1.7'>If you did not request this, you can safely ignore this email. Your current password will remain unchanged.</p>" +
                        "<p style='margin:0;font-size:13px;color:" + TEXT2 + ";line-height:1.7'>For security, this reset link will expire soon.</p>" +
                        "</div>";

        return wrap("Reset your password request for Nexus Careers.", content);
    }
}


