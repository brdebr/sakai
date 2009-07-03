package org.sakaiproject.profile2.tool.pages.panels;



import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.ComponentVisualErrorBehaviour;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.FeedbackLabel;
import org.sakaiproject.profile2.tool.models.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

public class MyContactEdit extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInfoEdit.class);
    private transient SakaiProxy sakaiProxy;

    public MyContactEdit(final String id, final UserProfile userProfile) {
		super(id);
		
        log.debug("MyContactEdit()");

        //get API's
		sakaiProxy = getSakaiProxy();
		
		//this panel
		final Component thisPanel = this;
			
		//get userId
		final String userId = userProfile.getUserId();
		
		
		//heading
		add(new Label("heading", new ResourceModel("heading.contact.edit")));
		
		//setup form	
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
	   	
	    // FeedbackPanel
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        // filteredErrorLevels will not be shown in the FeedbackPanel
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
	    
		//email
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		TextField email = new TextField("email", new PropertyModel(userProfile, "email"));
		email.add(EmailAddressValidator.getInstance());
		//readonly view
		Label emailReadOnly = new Label("emailReadOnly", new PropertyModel(userProfile, "email"));
		
		if(sakaiProxy.isAccountUpdateAllowed(userId)) {
			emailReadOnly.setVisible(false);
		} else {
			email.setVisible(false);
		}
		emailContainer.add(email);
		emailContainer.add(emailReadOnly);
		
		//email feedback
        final FeedbackLabel emailFeedback = new FeedbackLabel("emailFeedback", email);
        emailFeedback.setOutputMarkupId(true);
        emailContainer.add(emailFeedback);
        email.add(new ComponentVisualErrorBehaviour("onblur", emailFeedback));
		form.add(emailContainer);
		
		//homepage
		WebMarkupContainer homepageContainer = new WebMarkupContainer("homepageContainer");
		homepageContainer.add(new Label("homepageLabel", new ResourceModel("profile.homepage")));
		TextField homepage = new TextField("homepage", new PropertyModel(userProfile, "homepage")) {
			
			private static final long serialVersionUID = 1L; 

            // add http:// if missing 
            @Override 
            protected void convertInput() { 
                    String input = getInput(); 

                    if (StringUtils.isNotBlank(input) && !(input.startsWith("http://") || input.startsWith("https://"))) { 
                            setConvertedInput("http://" + input); 
                    } else { 
                            setConvertedInput(StringUtils.isBlank(input) ? null : input); 
                    } 
            } 
		};
		homepage.add(new UrlValidator());
		homepageContainer.add(homepage);
		
		//homepage feedback
        final FeedbackLabel homepageFeedback = new FeedbackLabel("homepageFeedback", homepage);
        homepageFeedback.setOutputMarkupId(true);
        homepageContainer.add(homepageFeedback);
        homepage.add(new ComponentVisualErrorBehaviour("onblur", homepageFeedback));
		form.add(homepageContainer);
		
		//workphone
		WebMarkupContainer workphoneContainer = new WebMarkupContainer("workphoneContainer");
		workphoneContainer.add(new Label("workphoneLabel", new ResourceModel("profile.phone.work")));
		TextField workphone = new TextField("workphone", new PropertyModel(userProfile, "workphone"));
		workphoneContainer.add(workphone);
		form.add(workphoneContainer);
		
		//homephone
		WebMarkupContainer homephoneContainer = new WebMarkupContainer("homephoneContainer");
		homephoneContainer.add(new Label("homephoneLabel", new ResourceModel("profile.phone.home")));
		TextField homephone = new TextField("homephone", new PropertyModel(userProfile, "homephone"));
		homephoneContainer.add(homephone);
		form.add(homephoneContainer);
		
		//mobilephone
		WebMarkupContainer mobilephoneContainer = new WebMarkupContainer("mobilephoneContainer");
		mobilephoneContainer.add(new Label("mobilephoneLabel", new ResourceModel("profile.phone.mobile")));
		TextField mobilephone = new TextField("mobilephone", new PropertyModel(userProfile, "mobilephone"));
		mobilephoneContainer.add(mobilephone);
		form.add(mobilephoneContainer);
		
		//facsimile
		WebMarkupContainer facsimileContainer = new WebMarkupContainer("facsimileContainer");
		facsimileContainer.add(new Label("facsimileLabel", new ResourceModel("profile.phone.facsimile")));
		TextField facsimile = new TextField("facsimile", new PropertyModel(userProfile, "facsimile"));
		facsimileContainer.add(facsimile);
		form.add(facsimileContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel

				if(save(form)) {
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_CONTACT_UPDATE, "/profile/"+userProfile.getUserId(), true);
					
					//repaint panel
					Component newPanel = new MyContactDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.addComponent(newPanel);
						//resize iframe
						target.appendJavascript("setMainFrameHeight(window.name);");
					}
				
				} else {
					String js = "alert('Failed to save information. Contact your system administrator.');";
					target.prependJavascript(js);
				}
				
				
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyContactDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
		
		//add form to page
		add(form);
		
	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get sakaiProxy, then get userId from sakaiProxy, then get sakaiperson for that userId
		SakaiProxy sakaiProxy = getSakaiProxy();
		
		String userId = sakaiProxy.getCurrentUserId();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
	
		//set the attributes from userProfile that this form dealt with, into sakaiPerson
		//this WILL fail if there is no sakaiPerson for the user however this should have been caught already
		//as a new Sakaiperson for a user is created in MyProfile if they don't have one.
		
		//sakaiPerson.setMail(userProfile.getEmail()); //email
		sakaiPerson.setLabeledURI(userProfile.getHomepage()); //homepage
		sakaiPerson.setTelephoneNumber(userProfile.getWorkphone()); //workphone
		sakaiPerson.setHomePhone(userProfile.getHomephone()); //homephone
		sakaiPerson.setMobile(userProfile.getMobilephone()); //mobilephone
		sakaiPerson.setFacsimileTelephoneNumber(userProfile.getFacsimile()); //facsimile

		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			
			//update their email address in their account if allowed
			if(sakaiProxy.isAccountUpdateAllowed(userId)) {
				sakaiProxy.updateEmailForUser(userId, userProfile.getEmail());
			}
						
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyContactEdit has been deserialized.");
		//re-init our transient objects
		sakaiProxy = getSakaiProxy();
	}

	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}
	
}
